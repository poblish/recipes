/**
 *
 */
package uk.co.recipes.service.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.myrrix.MyrrixUpdater;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static uk.co.recipes.metrics.MetricNames.*;

/**
 * TODO
 *
 * @author andrewregan
 */
@Singleton
public class MyrrixRecommendationService implements IRecommendationsAPI {

    @Inject
    MyrrixTasteRecommendationService tasteRecommendations;
    @Inject
    ClientRecommender recommender;
    @Inject
    EsItemFactory itemsFactory;
    @Inject
    EsRecipeFactory recipesFactory;
    @Inject
    MyrrixUpdater myrrixUpdater;
    @Inject
    MetricRegistry metrics;
    @Inject
    ObjectMapper mapper;

    private static final long[] ANON_EMPTYITEMS = {0L};
    private static final float[] ANON_EMPTYVALUES = {0};

    private static final Logger LOG = LoggerFactory.getLogger(MyrrixRecommendationService.class);

    @Inject
    public MyrrixRecommendationService() {
        // For Dagger
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IUser, int)
     */
    @Override
    public List<ICanonicalItem> recommendIngredients(final IUser inUser, int inNumRecs) {
        final Context timerCtxt = metrics.timer(TIMER_USER_ITEMS_RECOMMENDATIONS).time();

        try {
            return recommendIngredientsForId(inUser.getId(), inNumRecs);
        } finally {
            timerCtxt.stop();
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendIngredients(uk.co.recipes.api.IRecipe, int)
     */
    @Override
    public List<ICanonicalItem> recommendIngredients(final IRecipe inRecipe, int inNumRecs) {
        final Context timerCtxt = metrics.timer(TIMER_RECIPE_ITEMS_RECOMMENDATIONS).time();

        try {
            return recommendIngredientsForId(inRecipe.getId(), inNumRecs);
        } finally {
            timerCtxt.stop();
        }
    }

    private List<ICanonicalItem> recommendIngredientsForId(final long inUserOrRecipeId, int inNumRecs) {
        try {
            return itemsFactory.getAll(MyrrixUtils.getItems(recommender.recommend(inUserOrRecipeId, inNumRecs, false, new String[]{"ITEM"})));
        } catch (NoSuchUserException e) {
            return Collections.emptyList();
        } catch (TasteException | IOException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IRecommendationsAPI#recommendRecipes(uk.co.recipes.api.IUser, int)
     */
    @Override
    public List<IRecipe> recommendRecipes(IUser inUser, int inNumRecs) {
        final Context timerCtxt = metrics.timer(TIMER_RECIPES_RECOMMENDATIONS).time();

        try {
            return recipesFactory.getAll(MyrrixUtils.getItems(recommender.recommend(inUser.getId(), inNumRecs, false, new String[]{"RECIPE"})));
        } catch (NoSuchUserException e) {
            return Collections.emptyList();
        } catch (TasteException | IOException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        } finally {
            timerCtxt.stop();
        }
    }

    @Override
    public List<IRecipe> recommendRecipes(IUser inUser, int inNumRecs, ICanonicalItem... inIncludes) {
        if (inIncludes.length == 0) {
            return recommendRecipes(inUser, inNumRecs);
        }

        final Context timerCtxt = metrics.timer(TIMER_RECIPES_FILTERED_RECOMMENDATIONS).time();

        try {

            return recipesFactory.getAll(MyrrixUtils.getItems(recommender.recommend(inUser.getId(), inNumRecs, false, new String[]{"RECIPE", "", getItemsListJson(Arrays.asList(inIncludes))})));
        } catch (NoSuchUserException e) {
            return Collections.emptyList();
        } catch (TasteException | IOException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        } finally {
            timerCtxt.stop();
        }
    }

    @Override
    public List<IRecipe> recommendRecipesToAnonymous(int inNumRecs, ICanonicalItem... inIncludes) {
        return recommendRecipesToAnonymous(ANON_EMPTYITEMS, ANON_EMPTYVALUES, inNumRecs, null);
    }

    @Override
    public List<IRecipe> recommendRandomRecipesToAnonymous(final IRecipe inRecipe, int inNumRecs) {
        final List<IRecipe> recipesToChooseFrom = recommendRecipesToAnonymous(inRecipe, inNumRecs * 3);
        Collections.shuffle(recipesToChooseFrom);
        return recipesToChooseFrom.subList(0, Math.min(inNumRecs, recipesToChooseFrom.size()));
    }

    @Override
    public List<IRecipe> recommendRecipesToAnonymous(final IRecipe inUsersCreatedRecipe, int inNumRecs) {
        final Collection<ICanonicalItem> usersItems = inUsersCreatedRecipe.getItems();
        if (usersItems.isEmpty()) {
            return Collections.emptyList();  // If _no_ Items in Recipe, we cannot recommend anything. How the caller deals with that is his business.
        }

        //////////////////////////////////////////////////////////////  Build up the fake Item preferences

        // FIXME - Try to share with Ids-building code in EsExplorerFilters
        final long[] ids = new long[usersItems.size()];
        int j = 0;

        for (ICanonicalItem each : usersItems) {
            ids[j++] = each.getId();
        }

//		System.out.println("fake pref Item ids: " + Arrays.toString(ids));

        final float[] vals = new float[usersItems.size()];
        Arrays.fill(vals, 1.0f);

        //////////////////////////////////////////////////////////////

        return recommendRecipesToAnonymous(ids, vals, inNumRecs, usersItems);
    }

    private List<IRecipe> recommendRecipesToAnonymous(final long[] preferredItemIds, final float[] itemScores, int inNumRecs, final Collection<ICanonicalItem> inItems) {
        final Context timerCtxt = metrics.timer(TIMER_RECIPES_FILTERED_RECOMMENDATIONS).time();  // Same again - that OK?

        try {
            return recipesFactory.getAll(MyrrixUtils.getItems(recommender.recommendToAnonymous(preferredItemIds, itemScores, inNumRecs, new String[]{"RECIPE", "", getItemsListJson(inItems)}, null)));
        } catch (NoSuchUserException e) {
            // Basically, this means we've passed in one single Item pref, and that Item has had _zero_ previous prefs. In which case, we're quite entitled to return nothing. How the caller deals with that is his business.
            return Collections.emptyList();
        } catch (TasteException e) {
            LOG.error("FIXME > Ignore error for now: " + e);
            return Collections.emptyList();
            // FIXME throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        } catch (IOException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        } finally {
            timerCtxt.stop();
        }
    }

    private String getItemsListJson(final Collection<ICanonicalItem> inItems) throws IOException {
        if (inItems == null || inItems.isEmpty()) {
            return "";
        }

        return mapper.writeValueAsString(inItems);
    }

    @Override
    public List<Long> recommendIngredients(long inUser, int inNumRecs) {
        return tasteRecommendations.recommendIngredients(inUser, inNumRecs);
    }

    @Override
    public List<Long> recommendRecipes(long inUser, int inNumRecs) {
        return tasteRecommendations.recommendRecipes(inUser, inNumRecs);
    }
}