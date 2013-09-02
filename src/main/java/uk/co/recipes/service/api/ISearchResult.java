/**
 * 
 */
package uk.co.recipes.service.api;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface ISearchResult<T> {

	long getId();

	String getDisplayName();
	String getType();

    T getEntity();
}