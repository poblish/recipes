/**
 * 
 */
package uk.co.recipes.parse;

import javax.inject.Singleton;
import javax.inject.Named;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;

/**
 * FIXME This needs to be in a DSL, configurable, or something
 *
 * @author andrewregan
 *
 */
@Singleton
public class NameAdjuster {

    @Inject
    @Named("prefixAdjustments")
    List<String> badPrefixes;

	// FIXME Can probably replace with one big regex
	public AdjustedName adjust( final String inName) {
	    Collection<String> notesToAdd = Lists.newArrayList();
		String theNameToUse = inName.toLowerCase();
		int incr = 0;

		while (true) {
			boolean anyDoneThisRound = false;
			for ( String eachPrefix : badPrefixes) {
				if (theNameToUse.startsWith(eachPrefix + " ")) {
					incr += eachPrefix.length() + 1;
					theNameToUse = theNameToUse.substring( eachPrefix.length() + 1);
					notesToAdd.add(eachPrefix);
					anyDoneThisRound = true;
				}
			}

			if (!anyDoneThisRound) {
				break;
			}
		}

		return new AdjustedName( inName.substring(incr), notesToAdd);
	}
}
