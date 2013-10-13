/**
 * 
 */
package uk.co.recipes.service.api;

import java.util.Set;

import uk.co.recipes.api.ITag;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IExplorerFilterDef {

	Set<ITag> getIncludeTags();
	Set<ITag> getExcludeTags();
}