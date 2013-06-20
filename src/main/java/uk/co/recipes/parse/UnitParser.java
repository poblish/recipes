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

		final String s = inUnitsStr.trim().toUpperCase();

		if (s.equals("G")) {
			return Units.GRAMMES;
		}

		if (s.equals("BUNCH")) {
			return Units.BUNCHES;
		}

		if (s.equals("SPLASH")) {
			return Units.SPLASHES;
		}

		return Units.valueOf( inUnitsStr.trim().toUpperCase() );
	}
}
