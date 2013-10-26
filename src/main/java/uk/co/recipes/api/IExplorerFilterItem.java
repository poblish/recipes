/**
 * 
 */
package uk.co.recipes.api;

import com.google.common.base.Optional;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IExplorerFilterItem<T> {

    T getEntity();
    Optional<String> getValue();
}
