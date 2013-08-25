/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;
import java.util.Set;

import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUserPreferences;

import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class UserPreferences implements IUserPreferences {

	private Set<ITag> includeTags = Sets.newHashSet();
	private Set<ITag> excludeTags = Sets.newHashSet();

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUserPreferences#explorerIncludeTags()
	 */
	@Override
	public Collection<ITag> getExplorerIncludeTags() {
		return includeTags;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUserPreferences#explorerExcludeTags()
	 */
	@Override
	public Collection<ITag> getExplorerExcludeTags() {
		return excludeTags;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUserPreferences#explorerInclude(uk.co.recipes.api.ITag)
	 */
	@Override
	public void explorerInclude( ITag inTag) {
		includeTags.add(inTag);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUserPreferences#explorerExclude(uk.co.recipes.api.ITag)
	 */
	@Override
	public void explorerExclude( ITag inTag) {
		excludeTags.add(inTag);
	}
}
