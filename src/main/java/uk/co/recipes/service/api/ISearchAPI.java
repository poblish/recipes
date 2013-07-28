/**
 * 
 */
package uk.co.recipes.service.api;

import java.io.IOException;
import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ISearchAPI {

	List<ICanonicalItem> findItemsByName( final String inName) throws IOException;
	List<IRecipe> findRecipesByName( final String inName) throws IOException;
}
