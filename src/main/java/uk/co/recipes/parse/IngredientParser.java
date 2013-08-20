/**
 * 
 */
package uk.co.recipes.parse;

import static java.util.Locale.ENGLISH;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.NonNumericQuantities;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.tags.CommonTags;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientParser {

	@Inject
	EsItemFactory itemFactory;

    private static final String DEC_FRAC_NUMBER_BIT = "[0-9\\.]*(?: ?[0-9]/[0-9])?";
    private static final String DEC_FRAC_NUMBER_PATTERN = "(" + DEC_FRAC_NUMBER_BIT + ")";
    private static final String DEC_FRAC_NUMBER_RANGE_PATTERN = "(" + DEC_FRAC_NUMBER_BIT + "(?: ?- ?" + DEC_FRAC_NUMBER_BIT + ")?)";
	private static final String	NOTES = "([,;\\(].*)?";
	private static final String	SUFFIX = "([\\p{L}- ]*)" + NOTES;

	private static final Pattern	A = Pattern.compile( DEC_FRAC_NUMBER_RANGE_PATTERN + "( ?kg|g|gms| ?pounds?| ?lbs?\\.?| ?oz\\.?|cm|-in|-inch|mm|ml|l| litres?| ?quarts?| cups?| pots?| ?bunch(?:es)?| sticks?| heaped tbsps?| heaped tsps?| rounded tbsps?| rounded tsps?| tablespoons?| tbsp[s\\.]?| tsp[s\\.]?| teaspoons?| ?handfuls?| cloves?)? " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	B = Pattern.compile("((?:a )?(few |generous |good |large |small |thumb-sized? )?(splash|bunch|dash|drizzle|drops?|few|glass|handful|little|piece|knob|pinch|splash|squeeze)(?: of)?) " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	C = Pattern.compile("(juice|juice and zest|(?:finely )?(?:grated )?zest|zest and juice)(?: of)? " + DEC_FRAC_NUMBER_PATTERN + " " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	D = Pattern.compile("(icing sugar|nutmeg|parmesan|salt|salt and pepper.*|beaten egg|.*cream)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern    E = Pattern.compile("((?:dressed|steamed|cooked|sliced|sweet|roughly chopped) [\\w-\\(\\) ]*)" + NOTES, Pattern.CASE_INSENSITIVE);
    private static final Pattern    F = Pattern.compile(SUFFIX, Pattern.CASE_INSENSITIVE);

	public Optional<Ingredient> parse( final String inRawStr) {

	    final String adjustedStr = new FractionReplacer().replaceFractions(inRawStr);

		Matcher m = A.matcher(adjustedStr);
		if (m.matches()) {
			final NameAdjuster na = new NameAdjuster();

			String numericQuantityStr = m.group(1);
			if (numericQuantityStr.isEmpty()) {  // "handful" == "1 handful"
				numericQuantityStr = "1";
			}

			final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(3).trim() ) ), new Quantity( UnitParser.parse( m.group(2) ), NumericAmountParser.parse(numericQuantityStr)));

			final String note = m.group(4);
			if ( note != null) {
				ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
			}

			ingr.addNotes( ENGLISH, na.getExtraNotes());

			return Optional.of(ingr);
		}
		else {
			m = B.matcher(adjustedStr);
			if (m.matches()) {
				final NameAdjuster na = new NameAdjuster();

				final Quantity q;

				final String nonNumericQ = m.group(2);
				if ( nonNumericQ == null) {
					q = new Quantity( UnitParser.parse( m.group(3) ), 1);
				}
				else {
					q = new Quantity( UnitParser.parse( m.group(3) ), NonNumericQuantityParser.parse( nonNumericQ.trim().toUpperCase() ));
				}

				final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(4).trim() ) ), q);

				final String note = m.group(5);
				if ( note != null) {
					ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
				}

				ingr.addNotes( ENGLISH, na.getExtraNotes());

				return Optional.of(ingr);
			}
			else {
				m = C.matcher(adjustedStr);
				if (m.matches()) {
					final NameAdjuster na = new NameAdjuster();
					final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(3).trim() ) ), new Quantity( Units.INSTANCES, NumericAmountParser.parse( m.group(2) )));
					ingr.addNote( ENGLISH, m.group(1).trim());
					ingr.addNotes( ENGLISH, na.getExtraNotes());

					return Optional.of(ingr);
				}
				else {
					m = D.matcher(adjustedStr);
					if (m.matches()) {
						final NameAdjuster na = new NameAdjuster();
						final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(1).trim() ) ), new Quantity( Units.INSTANCES, 1));

						final String note = m.group(2);
						if ( note != null) {
							ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
						}

						ingr.addNotes( ENGLISH, na.getExtraNotes());

						return Optional.of(ingr);
					}
					else {
						m = E.matcher(adjustedStr);
						if (m.matches()) {
							final NameAdjuster na = new NameAdjuster();
							final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(1).trim() ) ), new Quantity( Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT));

							final String note = m.group(2);
							if ( note != null) {
								ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
							}

							ingr.addNotes( ENGLISH, na.getExtraNotes());

							return Optional.of(ingr);
						}
	                    else {
	                        m = F.matcher(adjustedStr);
	                        if (m.matches()) {
	                            final NameAdjuster na = new NameAdjuster();
	                            final Ingredient ingr = new Ingredient( findItem( na.adjust( m.group(1).trim() ) ), new Quantity( Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT));

	                            final String note = m.group(2);
	                            if ( note != null) {
	                                ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
	                            }

	                            ingr.addNotes( ENGLISH, na.getExtraNotes());
	                            System.out.println("... " + ingr);

	                            return Optional.of(ingr);
	                        }
	                    }
					}
				}
			}
		}

		return Optional.absent();
	}

	private ICanonicalItem findItem( final String inName) {
		return itemFactory.getOrCreate( inName, new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem item = new CanonicalItem(inName);

				// FIXME This stuff is OK, but not really sustainable
				String lcase = inName.toLowerCase();

				if ( lcase.endsWith("seeds") || lcase.endsWith("seed")) {
					item.addTag( CommonTags.SPICE );
				}

				return item;
			}
		}, true);
	}
}