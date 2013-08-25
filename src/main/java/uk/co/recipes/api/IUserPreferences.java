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
	void explorerInclude( final ITag inTag);
	void explorerExclude( final ITag inTag);

	Collection<ITag> getExplorerIncludeTags();
	Collection<ITag> getExplorerExcludeTags();
}
