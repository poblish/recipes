/**
 * 
 */
package uk.co.recipes.parse;

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
public class NameAdjuster {

    @Inject
    @Named("prefixAdjustments")
    List<String> badPrefixes;

	private final Collection<String> notesToAdd = Lists.newArrayList();

	// FIXME Can probably replace with one big regex
	public String adjust( final String inName) {
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

		return inName.substring(incr);
	}

	public Collection<String> getExtraNotes() {
		return notesToAdd;
	}
}
