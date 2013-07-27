/**
 * 
 */
package uk.co.recipes.service.api;

import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.taste.api.ITasteSimilarityAPI;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IExplorerAPI extends ITasteSimilarityAPI {

	List<ICanonicalItem> similarIngredients( final IUser inUser, final int inNumRecs);
	List<IRecipe> similarRecipes( final IUser inUser, final int inNumRecs);
}
