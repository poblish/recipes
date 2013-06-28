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
	private static final String[]	BAD_PREFIXES = {"beaten", "can", "chilled", "chopped", "coarsely", "cold", "crushed", "dark", "dressed", "dry", "dried", "fresh", "hot", "large", "plump", "small", "smoked", "tin", "whole"};

	private final Collection<String> notesToAdd = Lists.newArrayList();

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
