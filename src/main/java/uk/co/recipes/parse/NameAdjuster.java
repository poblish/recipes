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
	private static final String[]	BAD_PREFIXES = {"beaten", "boneless", "can", "chilled", "chopped", "coarse", "coarsely", "cold", "cooked", "cooking", "crushed", "dark", "dressed", "dry", "dried", "firm",
													"fresh", "freshly grated", "frozen", "full-bodied", "grated", "ground", "half-fat", "hot", "large", "lean", "long", "low-fat", "medium", "peeled", "pitted",
													"plump", "quality", "raw", "ripe", "roughly", "shelled", "skinless", "slices", "small", "smoked", "sustainable", "thick", "tin", "tinned", "toasted", "whole"};

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
