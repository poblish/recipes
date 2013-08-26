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
	// Return true if any change made
	boolean explorerIncludeAdd( final ITag inTag);
	boolean explorerIncludeRemove( final ITag inTag);
	boolean explorerExcludeAdd( final ITag inTag);
	boolean explorerExcludeRemove( final ITag inTag);
	boolean explorerClearAll();

	Collection<ITag> getExplorerIncludeTags();
	Collection<ITag> getExplorerExcludeTags();
}
