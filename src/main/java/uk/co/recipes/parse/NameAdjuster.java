/**
 *
 */
package uk.co.recipes.parse;

import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

/**
 * FIXME This needs to be in a DSL, configurable, or something
 *
 * @author andrewregan
 */
@Singleton
public class NameAdjuster {

    @Named("prefixAdjustments")
    @Inject
    List<String> badPrefixes;

    @Named("suffixAdjustments")
    @Inject
    List<String> badSuffixes;

    @Inject
    public NameAdjuster() {
        // For Dagger
    }

    // FIXME Can probably replace with one big regex
    public AdjustedName adjust(final String inName) {
        Collection<String> notesToAdd = Lists.newArrayList();
        String theNameToUse = inName.toLowerCase();
        int incr = 0;
        int endIdx = inName.length();

        while (true) {
            boolean anyDoneThisRound = false;
            for (String eachPrefix : badPrefixes) {
                if (theNameToUse.startsWith(eachPrefix + " ")) {
                    incr += eachPrefix.length() + 1;
                    theNameToUse = theNameToUse.substring(eachPrefix.length() + 1);
                    if (!eachPrefix.equals("x")) {  // Yuk, ugly bodge. There are plenty of these we want to just chuck away...
                        notesToAdd.add(eachPrefix);
                    }
                    anyDoneThisRound = true;
                }
            }

            for (String eachSuffix : badSuffixes) {
                if (theNameToUse.endsWith(" " + eachSuffix)) {
                    endIdx -= eachSuffix.length() + 1;
                    theNameToUse = theNameToUse.substring(0, theNameToUse.length() - eachSuffix.length() - 1);
                    notesToAdd.add(eachSuffix);
                    anyDoneThisRound = true;
                }
            }

            if (!anyDoneThisRound) {
                break;
            }
        }

        return new AdjustedName(inName.substring(incr, endIdx), notesToAdd);
    }
}
