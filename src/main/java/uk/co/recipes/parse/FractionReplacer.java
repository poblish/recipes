/**
 * 
 */
package uk.co.recipes.parse;

/**
 * Deal with Unicode character symbols before they get into the system
 * 
 * @author andrewr
 *
 */
public class FractionReplacer {

    public String replaceFractions( final String inStr) {
        return specialReplace( specialReplace( specialReplace( specialReplace( inStr.trim(), "⁄", "/"), "¼", ".25"), "¾", ".75"), "½", ".5");
    }

    private static String specialReplace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));

            // This stuff is mine...
            if (sb.length() == 0) {
                sb.append("0").append(newPattern);
            }
            else {
                final char prevChar = sb.charAt( sb.length() - 1 );
    
                if (Character.isDigit(prevChar)) {
                    sb.append(newPattern);
                }
                else if ( prevChar == ' ' && sb.length() > 1 && Character.isDigit( sb.charAt( sb.length() - 2 ) )) /* Fix '3 0.25' => '3.25' */ {
                    sb.deleteCharAt( sb.length() - 1); // delete previous space
                    sb.append(newPattern);
                }
                else if (Character.isLetter(prevChar)) {
                    throw new RuntimeException("Fractional character immediately follows letter");
                }
                else {
                    sb.append("0").append(newPattern);
                }
            }
            // My stuff ends

            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }

    private static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }
}
