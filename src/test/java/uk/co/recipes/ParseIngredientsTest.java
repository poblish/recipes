package uk.co.recipes;

import static java.util.Locale.ENGLISH;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;

import com.google.common.io.Files;

public class ParseIngredientsTest {

	static Pattern	A = Pattern.compile("([0-9]+)(g|ml| tbsp)? ([\\w- ]*)(?:, (.*))?", Pattern.CASE_INSENSITIVE);
	static Pattern	B = Pattern.compile("(small|large) (splash|bunch) (.*)(, (.*))*", Pattern.CASE_INSENSITIVE);
	static Pattern	C = Pattern.compile("(juice) ([0-9]+) (.*)(, (.*))*", Pattern.CASE_INSENSITIVE);
	static Pattern	D = Pattern.compile("(beaten egg)(?:, (.*))?", Pattern.CASE_INSENSITIVE);

	@Test
	public void parseIngredients() throws IOException {
		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/inputs.txt"), Charset.forName("utf-8"))) {
			Matcher m = A.matcher(eachLine);
			if (m.matches()) {
				IUnit x = ( m.group(2) != null) ? Units.valueOf( m.group(2).trim().toUpperCase() ) : Units.INSTANCES;

				final Ingredient ingr = new Ingredient( new NamedItem( new CanonicalItem( m.group(3) ) ), new Quantity( x, Integer.valueOf( m.group(1) )));

				if ( m.group(4) != null) {
					ingr.addNote( ENGLISH, m.group(4));
				}
				System.out.println(ingr);
			}
			else
			{
				m = B.matcher(eachLine);
				if (m.matches()) {
					System.out.println( m.group(1) + " / " + m.group(2) + " / " + m.group(3));
				}
				else
				{
					m = C.matcher(eachLine);
					if (m.matches()) {
						System.out.println( m.group(1) + " / " + m.group(2) + " / " + m.group(3));
					}
					else
					{
						m = D.matcher(eachLine);
						if (m.matches()) {
							System.out.println( m.group(1)+ " / " + m.group(2));
						}
						else
						{
							Assert.fail(eachLine + " not matched"); // System.err.println(eachLine);
						}
					}
				}
			}
		}
	}
}
