/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;
import java.util.Set;

import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUserPreferences;

import com.google.common.base.Objects;
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

	@Override
	public void explorerIncludeAdd( ITag inTag) {
		includeTags.add(inTag);
	}

	@Override
	public void explorerExcludeAdd( ITag inTag) {
		excludeTags.add(inTag);
	}

	@Override
	public void explorerIncludeRemove( ITag inTag) {
		includeTags.remove(inTag);
	}

	@Override
	public void explorerExcludeRemove( ITag inTag) {
		excludeTags.remove(inTag);
	}

	public String toString() {
		return Objects.toStringHelper(this)
						.add("includes", includeTags)
						.add("excludes", excludeTags)
						.toString();
	}
}
