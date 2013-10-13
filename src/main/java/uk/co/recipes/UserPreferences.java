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
	public boolean explorerIncludeAdd( ITag inTag) {
		explorerExcludeRemove(inTag);
		return includeTags.add(inTag);
	}

	@Override
	public boolean explorerExcludeAdd( ITag inTag) {
		explorerIncludeRemove(inTag);
		return excludeTags.add(inTag);
	}

	@Override
	public boolean explorerIncludeRemove( ITag inTag) {
		return includeTags.remove(inTag);
	}

	@Override
	public boolean explorerExcludeRemove( ITag inTag) {
		return excludeTags.remove(inTag);
	}

	@Override
	public boolean explorerClearAll() {
		boolean gotSome = !includeTags.isEmpty() || !excludeTags.isEmpty();
		includeTags.clear();
		excludeTags.clear();
		return gotSome;
	}

	public String toString() {
		return Objects.toStringHelper(this)
						.add("includes", includeTags)
						.add("excludes", excludeTags)
						.toString();
	}
}
