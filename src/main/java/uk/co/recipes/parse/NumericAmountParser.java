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
	public static int parse( final String inStr) {
        if (inStr.contains("-")) {  // OK, we've got a numeric range. For now (FIXME!) just use the average
            final int dashPos = inStr.indexOf("-");
            final String left = inStr.substring( 0, dashPos).trim();
            final String right = inStr.substring( dashPos + 1).trim();
            return (int) Math.round(( Double.valueOf(left) + Double.valueOf(right)) / 2);
        }
        else if (inStr.contains(".")) {
			Fraction f = new Fraction( Double.parseDouble(inStr) );
			return f.intValue();  // FIXME FIXME
		}
		else if (inStr.contains("/")) {
			int spos = inStr.indexOf(' ');
			int dpos = inStr.indexOf('/');
			String s2 = inStr.substring( spos + 1, spos+2);
			String s3 = inStr.substring( spos + 3);
			Fraction f = new Fraction( Integer.parseInt( inStr.substring( spos + 1, spos+2) ), Integer.parseInt( inStr.substring( spos + 3 )));
			return f.intValue();  // FIXME FIXME
		}
		return Integer.valueOf(inStr);
	}
}
