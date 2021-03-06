package uk.co.recipes.parse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.NonNumericQuantities;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.tags.CommonTags;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPE_LINE_PARSE;

public class IngredientParser {

    @Inject EsItemFactory itemFactory;
    @Inject NameAdjuster nameAdjuster;
    @Inject MetricRegistry metrics;
    @Inject OptionalNameSplitter optSplitter;

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IngredientParser.class);

    private static final String DEC_FRAC_NUMBER_BIT = "(?:[0-9\\.]+ ?x +)?[0-9\\.]*(?: ?[0-9]/[0-9])?";
    private static final String DEC_FRAC_NUMBER_PATTERN = "(" + DEC_FRAC_NUMBER_BIT + ")";
    private static final String DEC_FRAC_NUMBER_RANGE_PATTERN = "(?:About )?(" + DEC_FRAC_NUMBER_BIT + "(?: ?- ?" + DEC_FRAC_NUMBER_BIT + ")?)";
    private static final String NOTES = "([,;\\(].*)?";
    private static final String ITEM_NAME = "([\\p{L}- '&]*)";
    private static final String SUFFIX = ITEM_NAME + NOTES;

    private static final String NUMBER_AND_UNITS = DEC_FRAC_NUMBER_RANGE_PATTERN + "( ?(?:kg|ml|g) ?(?:bags?|cans?|cartons?|jar|packs?|pots?|punnets?|sachets?|tins?|tubs?)?|gms| ?pounds?| ?lbs?\\.?| ?oz\\.?|cm|-in|-inch|mm|ml| ?l| litres?| ?quarts?| bottles?| cups?| ?pots?| jars?| packets?| ?(?:big |large |small )?bunch(?:es)?| ?(?:big )?pinch(?:es)?| sticks?| heaped tbsps?| heaped tsps?| ?level tsps?| ?level tbsps?| rounded tbsps?| rounded tsps?| tablespoons?| tbsp[s\\.]?| tsp[s\\.]?| teaspoons?| ?handfuls?| cloves?)?";

    private static final Pattern NUMBER_AND_UNITS_PATTERN = Pattern.compile(NUMBER_AND_UNITS, Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEM_NAME_PATTERN = Pattern.compile(ITEM_NAME, Pattern.CASE_INSENSITIVE);

    private static final Pattern A = Pattern.compile(NUMBER_AND_UNITS + " " + SUFFIX, Pattern.CASE_INSENSITIVE);
    private static final Pattern B = Pattern.compile("((?:a )?(few |generous |good |large |small |thumb-sized? )?(bottle|bunch|dash|drizzle|drops?|few|glass|handful|little|piece|knob|pinch|quantity|splash|squeeze)(?: of)?) " + SUFFIX, Pattern.CASE_INSENSITIVE);
    private static final Pattern C = Pattern.compile("((?:finely )?(?:grated )?(?:juice|juice and zest|zest|zest and juice))(?: of)? " + DEC_FRAC_NUMBER_PATTERN + " " + SUFFIX, Pattern.CASE_INSENSITIVE);
    private static final Pattern D = Pattern.compile("(icing sugar|nutmeg|parmesan|salt|salt and pepper.*|beaten egg|.*cream)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern E = Pattern.compile("((?:dressed|steamed|cooked|sliced|sweet|roughly chopped) [\\w-\\(\\) ]*)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern F = Pattern.compile(SUFFIX, Pattern.CASE_INSENSITIVE);

    // Not very nice hacks...
    private static final Pattern GRAMMES_LBS_OZ_STRIPPER = Pattern.compile("g/ ?[0-9\\. lb]+ ?oz", Pattern.CASE_INSENSITIVE);
    private static final Pattern ML_FL_OZ_STRIPPER = Pattern.compile("ml/ ?[0-9\\.]+ ?fl oz", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTIWORD_COMMA_STRIPPER = Pattern.compile("(large|small|firm|frozen|thick|dried|boneless|skinless|raw|ripe|salted|unsalted),", Pattern.CASE_INSENSITIVE);
    private static final Pattern HALF_A_STRIPPER = Pattern.compile("^(Half|0.5)(?: an?)? ", Pattern.CASE_INSENSITIVE);  // Replace '0.5 A' and '0.5 an A', but don't replace '0.5 x A' - that causes problems. Horrible look-ahead...
    private static final Pattern WEAK_PREFIXES_STRIPPER = Pattern.compile("^(About|Approx|At least|Leaves from|Plus|Roughly|Up to) ", Pattern.CASE_INSENSITIVE);
    private static final Pattern CUPS_STRIPPER = Pattern.compile("[0-9] Cups?/([0-9]+ml)", Pattern.CASE_INSENSITIVE);

    @Inject
    public IngredientParser() {
        // For Dagger
    }

    public boolean parse(final String inRawStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
        try (Context ignored = metrics.timer(TIMER_RECIPE_LINE_PARSE).time()) {
            return timedParse(inRawStr, inHandler, inDeferHandler);
        }
    }

    private boolean timedParse(final String inRawStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {

        final String adjustedStr = adjustInput(inRawStr);

        Matcher m = A.matcher(adjustedStr);
        if (m.matches()) {
            String numericQuantityStr = m.group(1);
            if (numericQuantityStr.isEmpty()) {  // "handful" == "1 handful"
                numericQuantityStr = "1";
            }

            final AdjustedName adjusted = nameAdjuster.adjust(m.group(3).trim());
            final Quantity q = new Quantity(UnitParser.parse(m.group(2)), NumericAmountParser.parse(numericQuantityStr));
            final String note = m.group(4);

            // Yuk, refactor!

            if (adjusted.getName().contains(" or ")) {
                handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
            } else if (adjusted.getName().contains(" and ")) {
                handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
            } else {
                final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);

                if (note != null) {
                    ingr.addNote(ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);  // FIXME Factor this logic out!
                }

                ingr.addNotes(ENGLISH, adjusted.getNotes());

                inHandler.foundIngredient(ingr);
            }

            return true;
        } else {
            m = B.matcher(adjustedStr);
            if (m.matches()) {
                final Quantity q;

                final String nonNumericQ = m.group(2);
                if (nonNumericQ == null) {
                    q = new Quantity(UnitParser.parse(m.group(3)), 1);
                } else {
                    q = new Quantity(UnitParser.parse(m.group(3)), NonNumericQuantityParser.parse(nonNumericQ.trim().toUpperCase()));
                }

                final AdjustedName adjusted = nameAdjuster.adjust(m.group(4).trim());
                final String note = m.group(5);

                if (adjusted.getName().contains(" or ")) {
                    handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
                } else if (adjusted.getName().contains(" and ")) {
                    handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
                } else {
                    final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);

                    if (note != null) {
                        ingr.addNote(ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
                    }

                    ingr.addNotes(ENGLISH, adjusted.getNotes());

                    inHandler.foundIngredient(ingr);
                }

                return true;
            } else {
                m = C.matcher(adjustedStr);
                if (m.matches()) {
                    final AdjustedName adjusted = nameAdjuster.adjust(m.group(3).trim());
                    final IQuantity q = new Quantity(Units.INSTANCES, NumericAmountParser.parse(m.group(2)));
                    final String note = m.group(1).trim();

                    if (adjusted.getName().contains(" or ")) {
                        handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
                    } else if (adjusted.getName().contains(" and ")) {
                        handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
                    } else {
                        final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);
                        ingr.addNote(ENGLISH, note);
                        ingr.addNotes(ENGLISH, adjusted.getNotes());

                        inHandler.foundIngredient(ingr);
                    }

                    return true;
                } else {
                    m = D.matcher(adjustedStr);
                    if (m.matches()) {
                        final AdjustedName adjusted = nameAdjuster.adjust(m.group(1).trim());
                        final IQuantity q = new Quantity(Units.INSTANCES, 1);
                        final String note = m.group(2);

                        if (adjusted.getName().contains(" or ")) {
                            handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
                        } else if (adjusted.getName().contains(" and ")) {
                            handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
                        } else {
                            final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);

                            if (note != null) {
                                ingr.addNote(ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
                            }

                            ingr.addNotes(ENGLISH, adjusted.getNotes());

                            inHandler.foundIngredient(ingr);
                        }

                        return true;
                    } else {
                        m = E.matcher(adjustedStr);
                        if (m.matches()) {
                            final AdjustedName adjusted = nameAdjuster.adjust(m.group(1).trim());
                            final IQuantity q = new Quantity(Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT);
                            final String note = m.group(2);

                            if (adjusted.getName().contains(" or ")) {
                                handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
                            } else if (adjusted.getName().contains(" and ")) {
                                handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
                            } else {
                                final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);

                                if (note != null) {
                                    ingr.addNote(ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
                                }

                                ingr.addNotes(ENGLISH, adjusted.getNotes());

                                inHandler.foundIngredient(ingr);
                            }

                            return true;
                        } else {
                            m = F.matcher(adjustedStr);
                            if (m.matches()) {
                                final AdjustedName adjusted = nameAdjuster.adjust(m.group(1).trim());
                                final IQuantity q = new Quantity(Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT);
                                final String note = m.group(2);

                                if (adjusted.getName().contains(" or ")) {
                                    handleOredIngredients(adjusted, q, note, inHandler, inDeferHandler);
                                } else if (adjusted.getName().contains(" and ")) {
                                    handleAndedIngredients(adjusted, q, note, inHandler, inDeferHandler);
                                } else {
                                    final Ingredient ingr = new Ingredient(findItem(adjusted.getName()), q);

                                    if (note != null) {
                                        ingr.addNote(ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
                                    }

                                    ingr.addNotes(ENGLISH, adjusted.getNotes());

                                    inHandler.foundIngredient(ingr);
                                }

                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private String adjustInput(final String inRawStr) {
        String adjustedStr = new FractionReplacer().replaceFractions(inRawStr);
        adjustedStr = WEAK_PREFIXES_STRIPPER.matcher(adjustedStr).replaceAll("");  // "Approx 100g" -> "100g" is fine
        adjustedStr = GRAMMES_LBS_OZ_STRIPPER.matcher(adjustedStr).replaceAll("g");  // Strip stupid '200g/7oz' => '200g', '220g/1lb 7oz' => '220g'
        adjustedStr = ML_FL_OZ_STRIPPER.matcher(adjustedStr).replaceAll("ml");  // Strip stupid '200ml/7fl oz' => '200ml'
        adjustedStr = MULTIWORD_COMMA_STRIPPER.matcher(adjustedStr).replaceAll("$1");  // Pre-strip 'boneless, [skinless]' to work around our lame name parsing
        adjustedStr = CUPS_STRIPPER.matcher(adjustedStr).replaceAll("$1");  // Yuk, via CurryFrenzy

        if (!adjustedStr.contains(" x ")) {  // Ugly, prevents '0.5 x foo' => '0.5 x x foo'
            adjustedStr = HALF_A_STRIPPER.matcher(adjustedStr).replaceAll("0.5 x ");  // Yuk, 'Half a 400g can' -> '0.5 x 400g can' for easier parsing
        }

        return adjustedStr;
    }

    // FIXME Refactor!
    private void handleAndedIngredients(final AdjustedName inName, final IQuantity inQuantity, final String inNoteStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
        int idx = inName.getName().indexOf(" and ");

        // FIXME: should use Ngrams to prevent " lamb or beef stock" problem!
        final String name1 = inName.getName().substring(0, idx);
        final String name2 = inName.getName().substring(idx + 5);

        // Get all the possible combinations of names
        final SplitResults splitResults = optSplitter.split(name1, name2);
        // System.out.println("---> AND-splitting: " + splitResults);

        try {
            handleSplitIngredient(splitResults.getFirstResults(), false, inQuantity, inNoteStr, inName.getNotes(), inHandler, inDeferHandler);
            handleSplitIngredient(splitResults.getSecondResults(), false, inQuantity, inNoteStr, inName.getNotes(), inHandler, inDeferHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);  // Ugh
        }
    }

    // FIXME Refactor!
    private void handleOredIngredients(final AdjustedName inName, final IQuantity inQuantity, final String inNoteStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
        int idx = inName.getName().indexOf(" or ");

        // FIXME: should use Ngrams to prevent " lamb or beef stock" problem!
        final String name1 = inName.getName().substring(0, idx);
        final String name2 = inName.getName().substring(idx + 4);

        // Get all the possible combinations of names
        final SplitResults splitResults = optSplitter.split(name1, name2);
        // System.out.println("---> OR-splitting: " + splitResults);

        try {
            handleSplitIngredient(splitResults.getFirstResults(), true, inQuantity, inNoteStr, inName.getNotes(), inHandler, inDeferHandler);
            handleSplitIngredient(splitResults.getSecondResults(), true, inQuantity, inNoteStr, inName.getNotes(), inHandler, inDeferHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);  // Ugh
        }
    }

    private void handleSplitIngredient(final String[] namePossibilities, boolean isOptional, final IQuantity inQuantity,
                                       final String inNote, final Collection<String> inExtraNotes,
                                       final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) throws IOException {

        // Try the potentially best (longest) name only. If no match, defer, and then retry all possibilities (from best to worst) to maximise chance of getting match

        final Optional<ICanonicalItem> item1 = itemFactory.get(namePossibilities[0]);

        if (item1.isPresent()) {
            final Ingredient ingr1 = new Ingredient(item1.get(), inQuantity, isOptional);

            if (inNote != null) {
                ingr1.addNote(ENGLISH, /* FIXME: comma rubbish */ inNote.startsWith(",") ? inNote.substring(1).trim() : inNote);
            }

            ingr1.addNotes(ENGLISH, inExtraNotes);

            inHandler.foundIngredient(ingr1);
        } else if (inDeferHandler != null) {
            inDeferHandler.deferIngredient(new DeferralStatus(namePossibilities, inQuantity, inNote, inExtraNotes));
        } else {
            // Create???
        }
    }

    // For parsing a Quantity only
    public Optional<Quantity> parseQuantity(final String inRawStr) {
        final String adjustedStr = new FractionReplacer().replaceFractions(inRawStr);

        Matcher m = NUMBER_AND_UNITS_PATTERN.matcher(adjustedStr);
        if (m.matches()) {
            String numericQuantityStr = m.group(1);
            if (numericQuantityStr.isEmpty()) {  // "handful" == "1 handful"
                numericQuantityStr = "1";
            }
            return Optional.of(new Quantity(UnitParser.parse(m.group(2)), NumericAmountParser.parse(numericQuantityStr)));
        }

        return Optional.absent();
    }

    // For parsing an ingredient name only
    public boolean parseItemName(final String inRawStr) {
        return ITEM_NAME_PATTERN.matcher(adjustInput(inRawStr)).matches();
    }

    public ICanonicalItem findItem(final String inName) {
        return itemFactory.getOrCreate(inName, () -> {
            final ICanonicalItem item = new CanonicalItem(inName);

            // FIXME This stuff is OK, but not really sustainable
            String lcase = inName.toLowerCase();

            if (lcase.endsWith("seeds") || lcase.endsWith("seed")) {
                item.addTag(CommonTags.SPICE);
            } else if (lcase.endsWith(" flour")) {
                item.addTag(CommonTags.FLOUR);
            } else if (lcase.endsWith(" cheese")) {
                item.addTag(CommonTags.CHEESE);
            } else if (lcase.endsWith(" beans") || lcase.endsWith(" bean")) {
                item.addTag(CommonTags.PULSE);
            }

            return item;
        }, true);
    }
}