/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IUserPreferences {

	// FIXME These are pretty rubbish
	void explorerIncludeAdd( final ITag inTag);
	void explorerIncludeRemove( final ITag inTag);
	void explorerExcludeAdd( final ITag inTag);
	void explorerExcludeRemove( final ITag inTag);

	Collection<ITag> getExplorerIncludeTags();
	Collection<ITag> getExplorerExcludeTags();
}
