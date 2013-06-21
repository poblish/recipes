/**
 * 
 */
package uk.co.recipes.parse;

import static java.util.Locale.ENGLISH;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Ingredient;
import uk.co.recipes.NamedItem;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.NonNumericQuantities;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.CanonicalItemFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientParser {

	private static final String	NOTES = "([,\\(].*)?";
	private static final String	SUFFIX = "([\\w- ]*)" + NOTES;

	private static final Pattern	A = Pattern.compile("([0-9]+)(g|cm|mm|ml| heaped tbsp| tbsp| tsp)? " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	B = Pattern.compile("((small|large) (splash|bunch)) " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	C = Pattern.compile("(juice|zest) ([0-9]+) (.*)(, (.*))*", Pattern.CASE_INSENSITIVE);
	private static final Pattern	D = Pattern.compile("(beaten egg)" + NOTES, Pattern.CASE_INSENSITIVE);
	private static final Pattern	E = Pattern.compile("(dressed [\\w-\\(\\) ]*)" + NOTES, Pattern.CASE_INSENSITIVE);

	public static Optional<Ingredient> parse( final String inStr) {

		Matcher m = A.matcher(inStr);
		if (m.matches()) {

			final Ingredient ingr = new Ingredient( new NamedItem( findItem( m.group(3).trim() ) ), new Quantity( UnitParser.parse( m.group(2) ), Integer.valueOf( m.group(1) )));

			final String note = m.group(4);
			if ( note != null) {
				ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
			}

			return Optional.of(ingr);
		}
		else {
			m = B.matcher(inStr);
			if (m.matches()) {
				final Ingredient ingr = new Ingredient( new NamedItem( findItem( m.group(4).trim() ) ), new Quantity( UnitParser.parse( m.group(3) ), NonNumericQuantities.valueOf( m.group(2).trim().toUpperCase() )));

				final String note = m.group(5);
				if ( note != null) {
					ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
				}

				return Optional.of(ingr);
			}
			else {
				m = C.matcher(inStr);
				if (m.matches()) {
					final Ingredient ingr = new Ingredient( new NamedItem( findItem( m.group(3).trim() ) ), new Quantity( Units.INSTANCES, Integer.valueOf( m.group(2) )));
					ingr.addNote( ENGLISH, "Juice of");

					return Optional.of(ingr);
				}
				else {
					m = D.matcher(inStr);
					if (m.matches()) {
						final Ingredient ingr = new Ingredient( new NamedItem( findItem( m.group(1).trim() ) ), new Quantity( Units.INSTANCES, 1));
//							ingr.addNote( ENGLISH, "Beaten");

						final String note = m.group(2);
						if ( note != null) {
							ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
						}

						return Optional.of(ingr);
					}
					else {
						m = E.matcher(inStr);
						if (m.matches()) {
							final Ingredient ingr = new Ingredient( new NamedItem( findItem( m.group(1).trim() ) ), new Quantity( Units.INSTANCES, NonNumericQuantities.ANY_AMOUNT));

							final String note = m.group(2);
							if ( note != null) {
								ingr.addNote( ENGLISH, note.startsWith(",") ? note.substring(1).trim() : note);
							}

							return Optional.of(ingr);
						}
					}
				}
			}
		}

		return Optional.absent();
	}

	private static ICanonicalItem findItem( final String inName) {
		return CanonicalItemFactory.getOrCreate( inName, new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem item = new CanonicalItem(inName);

				String lcase = inName.toLowerCase();

				if ( lcase.endsWith("seeds") || lcase.endsWith("seed")) {
					item.addTag( CommonTags.SPICE, Boolean.TRUE);
				}
				else if ( lcase.endsWith(" oil")) {
					item.addTag( CommonTags.OIL, Boolean.TRUE);
				}

				return item;
			}} );
	}
}