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

        if (s.equals("G") || s.equals("GMS") || s.startsWith("G ")) {
            return Units.GRAMMES;
        }

		if (s.startsWith("TABLESPOON") || s.startsWith("LEVEL TBSP")) {
			return Units.TBSP;
		}

        if (s.startsWith("TSP") || s.startsWith("TEASPOON") || s.startsWith("LEVEL TSP")) {
            return Units.TSP;
        }

        if (s.startsWith("HEAPED TSP")) {
            return Units.HEAPED_TSP;
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

        if (s.startsWith("ML ")) {
            return Units.ML;
        }

        if (s.startsWith("KG ")) {
            return Units.KG;
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

		if (s.startsWith("BUNCH")) {
			return Units.BUNCHES;
		}

		if (s.startsWith("BIG BUNCH") || s.startsWith("LARGE BUNCH")) {
			return Units.BIG_BUNCHES;
		}

		if (s.startsWith("SMALL BUNCH")) {
			return Units.SMALL_BUNCHES;
		}

		if (s.startsWith("BIG PINCH")) {
			return Units.BIG_PINCH;
		}

        if (s.startsWith("PINCH")) {
            return Units.PINCH;
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
