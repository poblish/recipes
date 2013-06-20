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
import uk.co.recipes.api.NonNumericQuantities;
import uk.co.recipes.api.Units;

import com.google.common.base.Optional;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientParser {

	private static final String	NOTES = "(?:, (.*))?";
	private static final String	SUFFIX = "([\\w- ]*)" + NOTES;

	private static final Pattern	A = Pattern.compile("([0-9]+)(g|ml| tbsp)? " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	B = Pattern.compile("((small|large) (splash|bunch)) " + SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern	C = Pattern.compile("(juice) ([0-9]+) (.*)(, (.*))*", Pattern.CASE_INSENSITIVE);
	private static final Pattern	D = Pattern.compile("(beaten egg)" + NOTES, Pattern.CASE_INSENSITIVE);

	public static Optional<Ingredient> parse( final String inStr) {

		Matcher m = A.matcher(inStr);
		if (m.matches()) {
			final Ingredient ingr = new Ingredient( new NamedItem( new CanonicalItem( m.group(3) ) ), new Quantity( UnitParser.parse( m.group(2) ), Integer.valueOf( m.group(1) )));

			if ( m.group(4) != null) {
				ingr.addNote( ENGLISH, m.group(4));
			}

			return Optional.of(ingr);
		}
		else
		{
			m = B.matcher(inStr);
			if (m.matches()) {
				final Ingredient ingr = new Ingredient( new NamedItem( new CanonicalItem( m.group(4) ) ), new Quantity( UnitParser.parse( m.group(3) ), NonNumericQuantities.valueOf( m.group(2).trim().toUpperCase() )));

				if ( m.group(5) != null) {
					ingr.addNote( ENGLISH, m.group(5));
				}

				return Optional.of(ingr);
			}
			else
			{
				m = C.matcher(inStr);
				if (m.matches()) {
					final Ingredient ingr = new Ingredient( new NamedItem( new CanonicalItem( m.group(3) ) ), new Quantity( Units.INSTANCES, Integer.valueOf( m.group(2) )));
					ingr.addNote( ENGLISH, "Juice of");

					return Optional.of(ingr);
				}
				else
				{
					m = D.matcher(inStr);
					if (m.matches()) {
						final Ingredient ingr = new Ingredient( new NamedItem( new CanonicalItem( m.group(1) ) ), new Quantity( Units.INSTANCES, 1));
//							ingr.addNote( ENGLISH, "Beaten");
						ingr.addNote( ENGLISH, m.group(2));

						return Optional.of(ingr);
					}
				}
			}
		}

		return Optional.absent();
	}
}
