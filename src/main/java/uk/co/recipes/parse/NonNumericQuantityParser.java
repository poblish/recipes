/**
 *
 */
package uk.co.recipes.parse;

import uk.co.recipes.api.NonNumericQuantities;

/**
 * TODO
 *
 * @author andrewregan
 */
public class NonNumericQuantityParser {

    public static NonNumericQuantities parse(final String inStr) {
        if (inStr.startsWith("THUMB-SIZE")) {
            return NonNumericQuantities.THUMB_SIZE;
        }

        return NonNumericQuantities.valueOf(inStr);
    }
}