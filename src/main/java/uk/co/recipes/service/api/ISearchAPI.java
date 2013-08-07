/**
 * 
 */
package uk.co.recipes.service.api;

import java.io.IOException;
import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ISearchAPI {

	List<ICanonicalItem> findItemsByName( final String inName) throws IOException;
	int countItemsByName( final String inName) throws IOException;

	List<IRecipe> findRecipesByName( final String inName) throws IOException;
	int countRecipesByName( final String inName) throws IOException;

    List<ICanonicalItem> findItemsByTag( final ITag inTag) throws IOException;
    int countItemsByTag( final ITag inTag) throws IOException;

    List<IRecipe> findRecipesByTag( final ITag inTag) throws IOException;
    int countRecipesByTag( final ITag inTag) throws IOException;
}
