/**
 * 
 */
package uk.co.recipes.service.api;

import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.taste.api.ITasteRecommendationsAPI;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IRecommendationsAPI extends ITasteRecommendationsAPI {

	List<ICanonicalItem> recommendIngredients( final IRecipe inRecipe, final int inNumRecs);
	List<ICanonicalItem> recommendIngredients( final IUser inUser, final int inNumRecs);

	List<IRecipe> recommendRecipes( final IUser inUser, final int inNumRecs);
	List<IRecipe> recommendRecipes( final IUser inUser, final int inNumRecs, final ICanonicalItem... inIncludes);

	List<IRecipe> recommendRecipesToAnonymous( final IRecipe inRecipe, final int inNumRecs);
	List<IRecipe> recommendRecipesToAnonymous( final int inNumRecs, final ICanonicalItem... inIncludes);
}
