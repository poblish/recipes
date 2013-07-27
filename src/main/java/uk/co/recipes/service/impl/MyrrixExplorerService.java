/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.List;

import javax.inject.Inject;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixExplorerService implements IExplorerAPI {

	@Inject
	MyrrixTasteSimilarityService tasteSimilarity;

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IExplorerAPI#similarIngredients(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<ICanonicalItem> similarIngredients( final IUser inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IExplorerAPI#similarRecipes(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<IRecipe> similarRecipes( final IUser inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarIngredients(long, int)
	 */
	@Override
	public List<Long> similarIngredients( final long inUser, int inNumRecs) {
		return tasteSimilarity.similarIngredients( inUser, inNumRecs);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarRecipes(long, int)
	 */
	@Override
	public List<Long> similarRecipes( final long inUser, int inNumRecs) {
		return tasteSimilarity.similarRecipes( inUser, inNumRecs);
	}
}