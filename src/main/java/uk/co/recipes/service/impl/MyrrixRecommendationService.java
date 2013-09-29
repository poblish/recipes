/**
 *
 */
package uk.co.recipes.service.impl;

import static uk.co.recipes.metrics.MetricNames.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.google.common.collect.Iterables;
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

	private static final long[] ANON_EMPTYITEMS = new long[]{0L};
	private static final float[] ANON_EMPTYVALUES = new float[]{0};

	private final static Logger LOG = LoggerFactory.getLogger( MyrrixRecommendationService.class );

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
			if ( recipesToInclude.isEmpty()) {
				return Collections.emptyList();  // If _no_ Recipes contain the specified Items, we cannot recommend anything. How the caller deals with that is his business.
			}

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
		return recommendRecipesToAnonymous( ANON_EMPTYITEMS, ANON_EMPTYVALUES, inNumRecs, "");
	}

	@Override
	public List<IRecipe> recommendRecipesToAnonymous( final IRecipe inRecipe, int inNumRecs) {
		final Collection<ICanonicalItem> items = inRecipe.getItems();
		if ( items.isEmpty()) {
			return Collections.emptyList();  // If _no_ Items in Recipe, we cannot recommend anything. How the caller deals with that is his business.
		}

		final ICanonicalItem[] itemsArray = Iterables.toArray( items, ICanonicalItem.class);

        //////////////////////////////////////////////////////////////  Strip out Recipes that don't contain _all_ these Items

		try {
			final List<IRecipe> recipesToInclude = searchAPI.findRecipesByItemName(itemsArray);
			if ( recipesToInclude.isEmpty()) {
				return Collections.emptyList();  // If _no_ Recipes contain the specified Items, we cannot recommend anything. How the caller deals with that is his business.
			}

			// FIXME - Try to share with Ids-building code in EsExplorerFilters
            final long[] recipeIds = new long[ recipesToInclude.size() ];
            int i = 0;

            for ( IRecipe each : recipesToInclude) {
            	recipeIds[i++] = each.getId();
            }

			final String includeIdsStr = Longs.join( ",", recipeIds);

//			System.out.println("filter Recipe ids: " + includeIdsStr);

	        //////////////////////////////////////////////////////////////  Build up the fake Item preferences

			// FIXME - Try to share with Ids-building code in EsExplorerFilters
	        final long[] ids = new long[ items.size() ];
            int j = 0;

	        for ( ICanonicalItem each : items) {
	            ids[j++] = each.getId();
	        }

//			System.out.println("fake pref Item ids: " + Arrays.toString(ids));

	        final float[] vals = new float[ items.size() ];
	        Arrays.fill( vals, 1.0f);

	        //////////////////////////////////////////////////////////////
	
			return recommendRecipesToAnonymous( ids, vals, inNumRecs, includeIdsStr);
        }
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	private List<IRecipe> recommendRecipesToAnonymous( final long[] preferredItemIds,  final float[] itemScores, int inNumRecs, final String inIncludesStr) {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_FILTERED_RECOMMENDATIONS).time();  // Same again - that OK?

		try {
			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.recommendToAnonymous( preferredItemIds, itemScores, inNumRecs, new String[]{"RECIPE", inIncludesStr, ""}, null) ) );
		}
        catch (NoSuchUserException e) {
			// Basically, this means we've passed in one single Item pref, and that Item has had _zero_ previous prefs. In which case, we're quite entitled to return nothing. How the caller deals with that is his business.
            return Collections.emptyList();
        }
        catch (TasteException e) {
        	LOG.error("FIXME > Ignore error for now: ", e);
            return Collections.emptyList();
            // FIXME throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
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