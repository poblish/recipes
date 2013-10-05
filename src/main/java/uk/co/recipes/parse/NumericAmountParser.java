/**
 * 
 */
package uk.co.recipes.parse;

import org.apache.commons.math3.fraction.Fraction;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class NumericAmountParser {

	// FIXME Should use http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/fraction/Fraction.html
	public static int parse( final String inStrx) {
		String adjustedStr = inStrx;
        int multiplierPos = inStrx.indexOf('x');
        double multiplier = 1.0;

        if ( multiplierPos > 0) {
        	multiplier = Double.valueOf( adjustedStr.substring( 0, multiplierPos).trim() );
        	adjustedStr = adjustedStr.substring( multiplierPos + 1).trim();
        }

        if (adjustedStr.contains("-")) {  // OK, we've got a numeric range. For now (FIXME!) just use the average
            final int dashPos = adjustedStr.indexOf("-");
            final String left = adjustedStr.substring( 0, dashPos).trim();
            final String right = adjustedStr.substring( dashPos + 1).trim();
            return (int) Math.round(( Double.valueOf(left) + Double.valueOf(right)) / 2);
        }
        else if (adjustedStr.contains(".")) {
			Fraction f = new Fraction( Double.parseDouble(adjustedStr) );
			return f.intValue();  // FIXME FIXME
		}
		else if (adjustedStr.contains("/")) {
			int spos = adjustedStr.indexOf(' ');
			int dpos = adjustedStr.indexOf('/');
			String s2 = adjustedStr.substring( spos + 1, spos+2);
			String s3 = adjustedStr.substring( spos + 3);
			Fraction f = new Fraction( Integer.parseInt( adjustedStr.substring( spos + 1, spos+2) ), Integer.parseInt( adjustedStr.substring( spos + 3 )));
			return f.intValue();  // FIXME FIXME
		}

		return (int) (multiplier * Integer.valueOf(adjustedStr));
	}
}
