/**
 * 
 */
package uk.co.recipes.service.api;

import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.service.taste.api.ITasteSimilarityAPI;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IExplorerAPI extends ITasteSimilarityAPI {

	List<ICanonicalItem> similarIngredients( final ICanonicalItem item, final int inNumRecs);
	List<ICanonicalItem> similarIngredients( final ICanonicalItem item, final IExplorerFilter inFilter, final int inNumRecs);
	List<IRecipe> similarRecipes( final IRecipe recipe, final int inNumRecs);
	List<IRecipe> similarRecipes( final IRecipe recipe, final IExplorerFilter inFilter, final int inNumRecs);

	float similarity( final ICanonicalItem item1, final ICanonicalItem item2);
	float similarity( final IRecipe recipe1, final IRecipe recipe2);
}
