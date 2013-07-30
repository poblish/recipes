/**
 * 
 */
package uk.co.recipes.service.impl;

import uk.co.recipes.events.impl.MyrrixUpdater;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.common.base.Throwables;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.RecipeFactory;
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

	@Inject
	ClientRecommender recommender;

	@Inject
	CanonicalItemFactory itemsFactory;

	@Inject
	RecipeFactory recipesFactory;

	@Inject
	MyrrixUpdater myrrixUpdater;

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<ICanonicalItem> recommendIngredients( IUser inUser, int inNumRecs) {
		try {
			return itemsFactory.getAll( MyrrixUtils.getItems( recommender.recommend( inUser.getId(), inNumRecs, false, new String[]{"ITEM"}) ) );
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendRecipes(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<IRecipe> recommendRecipes( IUser inUser, int inNumRecs) {
		try {
			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.recommend( inUser.getId(), inNumRecs, false, new String[]{"RECIPE"}) ) );
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
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