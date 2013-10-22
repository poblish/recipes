/**
 * 
 */
package uk.co.recipes.service.api;

import java.util.Set;

import uk.co.recipes.api.IExplorerFilterItem;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IExplorerFilterDef {

	Set<IExplorerFilterItem<?>> getIncludes();
	Set<IExplorerFilterItem<?>> getExcludes();
}