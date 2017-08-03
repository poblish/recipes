/**
 *
 */
package uk.co.recipes.service.api;

import uk.co.recipes.api.IExplorerFilterItem;

import java.util.Set;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IExplorerFilterDef {

    Set<IExplorerFilterItem<?>> getIncludes();
    Set<IExplorerFilterItem<?>> getExcludes();
}