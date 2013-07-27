/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.List;

import javax.inject.Inject;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixRecommendationService implements IRecommendationsAPI {

	@Inject
	MyrrixTasteRecommendationService tasteRecommendations;

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<ICanonicalItem> recommendIngredients( IUser inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendRecipes(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<IRecipe> recommendRecipes( IUser inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}		

	@Override
	public List<Long> recommendIngredients( long inUser, int inNumRecs) {
		return tasteRecommendations.recommendIngredients( inUser, inNumRecs);
	}

	@Override
	public List<Long> recommendRecipes( long inUser, int inNumRecs) {
		return tasteRecommendations.recommendRecipes( inUser, inNumRecs);
	}
}