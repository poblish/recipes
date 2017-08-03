/**
 *
 */
package uk.co.recipes.service.api;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IExplorerFilter {

    long[] idsToInclude();
    long[] idsToExclude();
}