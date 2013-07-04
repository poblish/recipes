/**
 * 
 */
package uk.co.recipes.parse;

import java.util.Collection;

import com.google.common.collect.Lists;

/**
 * FIXME This needs to be in a DSL, configurable, or something
 *
 * @author andrewregan
 *
 */
public class NameAdjuster {
	private static final String[]	BAD_PREFIXES = {"beaten", "can", "chilled", "chopped", "coarse", "coarsely", "cold", "cooked", "crushed", "dark", "dressed", "dry", "dried", "firm", "fresh",
													"full-bodied", "hot", "large", "lean", "long", "medium", "plump", "quality", "raw", "skinless", "small", "smoked", "sustainable", "tin",
													"toasted", "whole"};

	private final Collection<String> notesToAdd = Lists.newArrayList();

	// FIXME Can probably replace with one big regex
	public String adjust( final String inName) {
		String theNameToUse = inName.toLowerCase();
		int incr = 0;

		while (true) {
			boolean anyDoneThisRound = false;
			for ( String eachPrefix : BAD_PREFIXES) {
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
