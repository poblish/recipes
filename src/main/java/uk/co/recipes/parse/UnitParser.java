/**
 * 
 */
package uk.co.recipes.parse;

import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public final class UnitParser {

    private UnitParser() {}

	public static IUnit parse( final String inUnitsStr) {
		if ( inUnitsStr == null || inUnitsStr.isEmpty()) {
			return Units.INSTANCES;
		}

		String s = inUnitsStr.trim().toUpperCase();

		if (s.startsWith("-")) {
			s = s.substring(1);
		}

		if (s.equals("G") || s.equals("GMS") || s.equals("G SACHET") || s.equals("G PACK") || s.equals("G TUB") || s.equals("G CARTON")) {
			return Units.GRAMMES;
		}

		if (s.startsWith("TABLESPOON")) {
			return Units.TBSP;
		}

		if (s.startsWith("TSP") || s.startsWith("TEASPOON")) {
			return Units.TSP;
		}

		if (s.startsWith("LB") || s.startsWith("POUND")) {
			return Units.POUNDS;
		}

		if (s.startsWith("OZ")) {
			return Units.OUNCES;
		}

		if (s.equals("L")) {
			return Units.LITRE;
		}

		if (s.equals("IN")) {
			return Units.INCH;
		}

		if (s.startsWith("CUP")) {
			return Units.CUP;
		}

		if (s.startsWith("POT")) {
			return Units.POT;
		}

		if (s.equals("BUNCH")) {
			return Units.BUNCHES;
		}

		if (s.equals("SPLASH")) {
			return Units.SPLASHES;
		}

		if (s.startsWith("HANDFUL")) {
			return Units.HANDFUL;
		}

		if (s.startsWith("KNOB")) {
			return Units.KNOB;
		}

		if (s.startsWith("CLOVE")) {
			return Units.CLOVE;
		}

		if (s.startsWith("STICK")) {
			return Units.STICK;
		}

		if (s.startsWith("DROP")) {
			return Units.DROP;
		}

		return Units.valueOf( s.replace(' ', '_') );
	}
}
