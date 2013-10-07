/**
 * 
 */
package uk.co.recipes.parse;

import static java.util.Locale.ENGLISH;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPE_PARSE;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.NonNumericQuantities;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.tags.CommonTags;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientParser {

	@Inject
	EsItemFactory itemFactory;

	@Inject NameAdjuster nameAdjuster;

	@Inject
	MetricRegistry metrics;

	@Inject OptionalNameSplitter optSplitter;

    private static final String DEC_FRAC_NUMBER_BIT = "(?:[0-9\\.]+ x )?[0-9\\.]*(?: ?[0-9]/[0-9])?";
    private static final String DEC_FRAC_NUMBER_PATTERN = "(" + DEC_FRAC_NUMBER_BIT + ")";
    private static final String DEC_FRAC_NUMBER_RANGE_PATTERN = "(?:About )?(" + DEC_FRAC_NUMBER_BIT + "(?: ?- ?" + DEC_FRAC_NUMBER_BIT + ")?)";
	private static final String	NOTES = "([,;\\(].*)?";
    private static final String ITEM_NAME = "([\\p{L}- '&]*)";
    private static final String SUFFIX = ITEM_NAME + NOTES;

    private static final String NUMBER_AND_UNITS = DEC_FRAC_NUMBER_RANGE_PATTERN + "( ?(?:kg|ml|g) ?(?:bag|cans?|carton|jar|pack|pot|punnet|sachet|tins?|tub)?|gms| ?pounds?| ?lbs?\\.?| ?oz\\.?|cm|-in|-inch|mm|ml| ?l| litres?| ?quarts?| cups?| ?pots?| jars?| packets?| ?(?:big |large |small )?bunch(?:es)?| ?(?:big )?pinch(?:es)?| sticks?| heaped tbsps?| heaped tsps?| ?level tsps?| ?level tbsps?| rounded tbsps?| rounded tsps?| tablespoons?| tbsp[s\\.]?| tsp[s\\.]?| teaspoons?| ?handfuls?| cloves?)?";

    private static final Pattern    NUMBER_AND_UNITS_PATTERN = Pattern.compile( NUMBER_AND_UNITS, Pattern.CASE_INSENSITIVE);
    private static final Pattern    ITEM_NAME_PATTERN = Pattern.compile( ITEM_NAME, Pattern.CASE_INSENSITIVE);

	private static final Pattern	A = Pattern.compile( NUMBER_AND_UNITS + " " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	B = Pattern.compile("((?:a )?(few |generous |good |large |small |thumb-sized? )?(splash|bunch|dash|drizzle|drops?|few|glass|handful|little|piece|knob|pinch|splash|squeeze)(?: of)?) " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	C = Pattern.compile("((?:finely )?(?:grated )?(?:juice|juice and zest|zest|zest and juice))(?: of)? " + DEC_FRAC_NUMBER_PATTERN + " " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	D = Pattern.compile("(icing sugar|nutmeg|parmesan|salt|salt and pepper.*|beaten egg|.*cream)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern    E = Pattern.compile("((?:dressed|steamed|cooked|sliced|sweet|roughly chopped) [\\w-\\(\\) ]*)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern    F = Pattern.compile(SUFFIX, Pattern.CASE_INSENSITIVE);


	public boolean parse( final String inRawStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPE_PARSE).time();

        try {
            return timedParse(inRawStr, inHandler, inDeferHandler);
        }
        finally {
            timerCtxt.stop();
        }
	}

	private boolean timedParse( final String inRawStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {

	    final String adjustedStr = new FractionReplacer().replaceFractions(inRawStr);

		Matcher m = A.matcher(adjustedStr);
		if (m.matches()) {
			String numericQuantityStr = m.group(1);
			if (numericQuantityStr.isEmpty()) {  // "handful" == "1 handful"
				numericQuantityStr = "1";
			}

            final AdjustedName adjusted = nameAdjuster.adjust( m.group(3).trim() );

            // Yuk, refactor!

            if ( adjusted.getName().contains(" or ")) {
            	int idx = adjusted.getName().indexOf(" or ");

            	// FIXME: should use Ngrams to prevent " lamb or beef stock" problem!
            	final String name1 = adjusted.getName().substring( 0, idx);
            	final String name2 = adjusted.getName().substring( idx + 4 );

            	// System.out.println( name1 + " / " + name2);
            	
            	final Quantity q = new Quantity( UnitParser.parse( m.group(2) ), NumericAmountParser.parse(numericQuantityStr));

            	// Get all the possible combinations of names
            	final SplitResults splitResults = optSplitter.split( name1, name2);

            	try {
            		handleOptionalIngredient( splitResults.getFirstResults(), q, m.group(4), adjusted.getNotes(), inHandler, inDeferHandler);
            		handleOptionalIngredient( splitResults.getSecondResults(), q, m.group(4), adjusted.getNotes(), inHandler, inDeferHandler);
				}
            	catch (IOException e) {
					Throwables.propagate(e);  // Ugh FIXME
				}
            }
            else {
				final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), new Quantity( UnitParser.parse( m.group(2) ), NumericAmountParser.parse(numericQuantityStr)));
	
				final String note = m.group(4);
				if ( note != null) {
					ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
				}
	
				ingr.addNotes( ENGLISH, adjusted.getNotes());
	
				inHandler.foundIngredient(ingr);
            }

			return true;
		}
		else {
			m = B.matcher(adjustedStr);
			if (m.matches()) {
				final Quantity q;

				final String nonNumericQ = m.group(2);
				if ( nonNumericQ == null) {
					q = new Quantity( UnitParser.parse( m.group(3) ), 1);
				}
				else {
					q = new Quantity( UnitParser.parse( m.group(3) ), NonNumericQuantityParser.parse( nonNumericQ.trim().toUpperCase() ));
				}

                final AdjustedName adjusted = nameAdjuster.adjust( m.group(4).trim() );
				final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), q);

				final String note = m.group(5);
				if ( note != null) {
					ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
				}

				ingr.addNotes( ENGLISH, adjusted.getNotes());

				inHandler.foundIngredient(ingr);
				return true;
			}
			else {
				m = C.matcher(adjustedStr);
				if (m.matches()) {
                    final AdjustedName adjusted = nameAdjuster.adjust( m.group(3).trim() );
					final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), new Quantity( Units.INSTANCES, NumericAmountParser.parse( m.group(2) )));
					ingr.addNote( ENGLISH, m.group(1).trim());
					ingr.addNotes( ENGLISH, adjusted.getNotes());

					inHandler.foundIngredient(ingr);
					return true;
				}
				else {
					m = D.matcher(adjustedStr);
					if (m.matches()) {
                        final AdjustedName adjusted = nameAdjuster.adjust( m.group(1).trim() );
						final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), new Quantity( Units.INSTANCES, 1));

						final String note = m.group(2);
						if ( note != null) {
							ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
						}

						ingr.addNotes( ENGLISH, adjusted.getNotes());

						inHandler.foundIngredient(ingr);
						return true;
					}
					else {
						m = E.matcher(adjustedStr);
						if (m.matches()) {
                            final AdjustedName adjusted = nameAdjuster.adjust( m.group(1).trim() );
							final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), new Quantity( Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT));

							final String note = m.group(2);
							if ( note != null) {
								ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
							}

							ingr.addNotes( ENGLISH, adjusted.getNotes());

							inHandler.foundIngredient(ingr);
							return true;
						}
	                    else {
	                        m = F.matcher(adjustedStr);
	                        if (m.matches()) {
	                            final AdjustedName adjusted = nameAdjuster.adjust( m.group(1).trim() );
	                            final Ingredient ingr = new Ingredient( findItem( adjusted.getName() ), new Quantity( Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT));

	                            final String note = m.group(2);
	                            if ( note != null) {
	                                ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
	                            }

	                            ingr.addNotes( ENGLISH, adjusted.getNotes());
//	                            System.out.println("... " + ingr);

	                			inHandler.foundIngredient(ingr);
	                			return true;
	                        }
	                    }
					}
				}
			}
		}

		return false;
	}

	private void handleOptionalIngredient( final String[] namePossibilities, final IQuantity inQuantity,
										   final String inNote, final Collection<String> inExtraNotes,
										   final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) throws IOException {

		// Try the potentially best (longest) name only. If no match, defer, and then retry all possibilities (from best to worst) to maximise chance of getting match

    	final Optional<ICanonicalItem> item1 = itemFactory.get( namePossibilities[0] );
    	
    	if (item1.isPresent()) {
			final Ingredient ingr1 = new Ingredient( item1.get(), inQuantity, Boolean.TRUE);

			if ( inNote != null) {
				ingr1.addNote( ENGLISH, /* FIXME: comma rubbish */ inNote.startsWith(",") ? inNote.substring(1).trim() : inNote);
			}

			ingr1.addNotes( ENGLISH, inExtraNotes);
			
			inHandler.foundIngredient(ingr1);
    	}
    	else if ( inDeferHandler != null) {
			inDeferHandler.deferIngredient( new DeferralStatus( namePossibilities, inQuantity, inNote, inExtraNotes) );
    	}
    	else {
    		// Create???
    	}
	}

	// For parsing a Quantity only
    public Optional<Quantity> parseQuantity( final String inRawStr) {
	    final String adjustedStr = new FractionReplacer().replaceFractions(inRawStr);

		Matcher m = NUMBER_AND_UNITS_PATTERN.matcher(adjustedStr);
		if (m.matches()) {
			String numericQuantityStr = m.group(1);
			if (numericQuantityStr.isEmpty()) {  // "handful" == "1 handful"
				numericQuantityStr = "1";
			}
			return Optional.of( new Quantity( UnitParser.parse( m.group(2) ), NumericAmountParser.parse(numericQuantityStr)) );
		}

		return Optional.absent();
    }

    // For parsing an ingredient name only
    public boolean parseItemName( final String inRawStr) {
        return ITEM_NAME_PATTERN.matcher(inRawStr).matches();
    }
    
	public ICanonicalItem findItem( final String inName) {
		return itemFactory.getOrCreate( inName, new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem item = new CanonicalItem(inName);

				// FIXME This stuff is OK, but not really sustainable
				String lcase = inName.toLowerCase();

				if ( lcase.endsWith("seeds") || lcase.endsWith("seed")) {
					item.addTag( CommonTags.SPICE );
				}
				else if (lcase.endsWith(" flour")) {
					item.addTag( CommonTags.FLOUR );
				}
				else if (lcase.endsWith(" cheese")) {
					item.addTag( CommonTags.CHEESE );
				}
				else if (lcase.endsWith(" beans") || lcase.endsWith(" bean")) {
					item.addTag( CommonTags.PULSE );
				}

				return item;
			}
		}, true);
	}
}