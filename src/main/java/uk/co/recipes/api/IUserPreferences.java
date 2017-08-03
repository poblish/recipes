/**
 *
 */
package uk.co.recipes.api;

import java.util.Collection;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IUserPreferences {

    // Return true if any change made
    boolean explorerIncludeAdd(final ITag inTag);
    boolean explorerIncludeAdd(final ITag inTag, final String inValue);
    boolean explorerIncludeAdd(final ICanonicalItem item);
    boolean explorerIncludeRemove(final ITag inTag);
    boolean explorerIncludeRemove(final ITag inTag, final String inValue);
    boolean explorerIncludeRemove(final ICanonicalItem item);
    boolean explorerExcludeAdd(final ITag inTag);
    boolean explorerExcludeAdd(final ITag inTag, final String inValue);
    boolean explorerExcludeAdd(final ICanonicalItem item);
    boolean explorerExcludeRemove(final ITag inTag);
    boolean explorerExcludeRemove(final ITag inTag, final String inValue);
    boolean explorerExcludeRemove(final ICanonicalItem item);
    boolean explorerClearAll();

    Collection<IExplorerFilterItem<?>> getExplorerIncludes();
    Collection<IExplorerFilterItem<?>> getExplorerExcludes();
}
