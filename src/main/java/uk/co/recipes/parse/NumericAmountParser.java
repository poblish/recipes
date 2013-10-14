/**
 * 
 */
package uk.co.recipes.parse;


/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class NumericAmountParser {

	// FIXME Should use http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/fraction/Fraction.html
	public static double parse( final String inStr) {
		String adjustedStr = inStr;
        int multiplierPos = inStr.indexOf('x');
        double multiplier = 1.0;

        if ( multiplierPos > 0) {
        	if (adjustedStr.contains("-")) {
                final int dashPos = adjustedStr.indexOf("-");
                final String left = adjustedStr.substring( 0, dashPos).trim();
                final String right = adjustedStr.substring( dashPos + 1, multiplierPos).trim();
                multiplier = ( Double.valueOf(left) + Double.valueOf(right)) / 2;
        		return multiplier * Integer.valueOf( adjustedStr.substring( multiplierPos + 1).trim() );
        	}
        	else {
        		multiplier = Double.valueOf( adjustedStr.substring( 0, multiplierPos).trim() );
        		adjustedStr = adjustedStr.substring( multiplierPos + 1).trim();
        	}
        }

        if (adjustedStr.contains("-")) {  // OK, we've got a numeric range. For now (FIXME!) just use the average
            final int dashPos = adjustedStr.indexOf("-");
            final String left = adjustedStr.substring( 0, dashPos).trim();
            final String right = adjustedStr.substring( dashPos + 1).trim();
            return ( Double.valueOf(left) + Double.valueOf(right)) / 2;
        }
        else if (adjustedStr.contains(".")) {
			return Double.parseDouble(adjustedStr);
		}
		else if (adjustedStr.contains("/")) {
			int spos = adjustedStr.indexOf(' ');
			return Double.valueOf( adjustedStr.substring( spos + 1, spos+2) ) / Double.valueOf( adjustedStr.substring( spos + 3) );
		}

		return multiplier * Integer.valueOf(adjustedStr);
	}
}
