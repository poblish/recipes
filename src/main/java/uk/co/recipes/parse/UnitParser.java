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
public class UnitParser {

	public static IUnit parse( final String inUnitsStr) {
		if ( inUnitsStr == null || inUnitsStr.isEmpty()) {
			return Units.INSTANCES;
		}

		String s = inUnitsStr.trim().toUpperCase();

		if (s.startsWith("-")) {
			s = s.substring(1);
		}

		if (s.equals("G") || s.equals("GMS")) {
			return Units.GRAMMES;
		}

		if (s.startsWith("TABLESPOON")) {
			return Units.TBSP;
		}

		if (s.startsWith("TSP") || s.startsWith("TEASPOON")) {
			return Units.TSP;
		}

		if (s.startsWith("LB")) {
			return Units.POUNDS;
		}

		if (s.startsWith("OZ")) {
			return Units.OUNCES;
		}

		if (s.equals("IN")) {
			return Units.INCH;
		}

		if (s.equals("CUPS")) {
			return Units.CUP;
		}

		if (s.equals("BUNCH")) {
			return Units.BUNCHES;
		}

		if (s.equals("SPLASH")) {
			return Units.SPLASHES;
		}

		if (s.startsWith("KNOB")) {
			return Units.KNOB;
		}

		if (s.startsWith("STICK")) {
			return Units.STICK;
		}

		return Units.valueOf( s.replace(' ', '_') );
	}
}
