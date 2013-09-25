/**
 *
 */
package uk.co.recipes.service.impl;

import static uk.co.recipes.metrics.MetricNames.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.google.common.primitives.Longs;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
@Singleton
public class MyrrixRecommendationService implements IRecommendationsAPI {

	@Inject MyrrixTasteRecommendationService tasteRecommendations;
	@Inject EsSearchService searchAPI;
	@Inject ClientRecommender recommender;
	@Inject EsItemFactory itemsFactory;
	@Inject EsRecipeFactory recipesFactory;
	@Inject MyrrixUpdater myrrixUpdater;
	@Inject MetricRegistry metrics;


	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<ICanonicalItem> recommendIngredients( final IUser inUser, int inNumRecs) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_USER_ITEMS_RECOMMENDATIONS).time();

		try {
			return recommendIngredientsForId( inUser.getId(), inNumRecs);
        }
        finally {
            timerCtxt.stop();
        }
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IRecipe, int)
	 */
	@Override
	public List<ICanonicalItem> recommendIngredients( final IRecipe inRecipe, int inNumRecs) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPE_ITEMS_RECOMMENDATIONS).time();

		try {
			return recommendIngredientsForId( inRecipe.getId(), inNumRecs);
        }
        finally {
            timerCtxt.stop();
        }
	}

	private List<ICanonicalItem> recommendIngredientsForId( final long inUserOrRecipeId, int inNumRecs) {
		try {
			return itemsFactory.getAll( MyrrixUtils.getItems( recommender.recommend( inUserOrRecipeId, inNumRecs, false, new String[]{"ITEM"}) ) );
		}
        catch (NoSuchUserException e) {
            return Collections.emptyList();
        }
        catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
        catch (IOException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendRecipes(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<IRecipe> recommendRecipes( IUser inUser, int inNumRecs) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_RECOMMENDATIONS).time();

		try {
			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.recommend( inUser.getId(), inNumRecs, false, new String[]{"RECIPE"}) ) );
		}
        catch (NoSuchUserException e) {
            return Collections.emptyList();
        }
        catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
        finally {
            timerCtxt.stop();
        }
	}

	@Override
	public List<IRecipe> recommendRecipes( IUser inUser, int inNumRecs, ICanonicalItem... inIncludes) {
		if (inIncludes.length == 0) {
			return recommendRecipes( inUser, inNumRecs);
		}

	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_FILTERED_RECOMMENDATIONS).time();

		try {
			final List<IRecipe> recipesToInclude = searchAPI.findRecipesByItemName(inIncludes);

			// FIXME - Try to share with Ids-building code in EsExplorerFilters
            final long[] ids = new long[ recipesToInclude.size() ];
            int i = 0;

            for ( IRecipe each : recipesToInclude) {
                ids[i++] = each.getId();
            }

			final String includeIdsStr = Longs.join( ",", ids);
			final String excludeIdsStr = "";

			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.recommend( inUser.getId(), inNumRecs, false, new String[]{"RECIPE", includeIdsStr, excludeIdsStr}) ) );
		}
        catch (NoSuchUserException e) {
            return Collections.emptyList();
        }
        catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
        finally {
            timerCtxt.stop();
        }
	}

	@Override
	public List<IRecipe> recommendRecipesToAnonymous( int inNumRecs, ICanonicalItem... inIncludes) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_FILTERED_RECOMMENDATIONS).time();  // Same again - that OK?

	    try {
			return Collections.emptyList();  // FIXME!!!
	    }
/*		try {
			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.recommendToAnonymous( inNumRecs, false, new String[]{"RECIPE"}) ) );
		}
        catch (NoSuchUserException e) {
            return Collections.emptyList();
        }
        catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		} */
        finally {
            timerCtxt.stop();
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