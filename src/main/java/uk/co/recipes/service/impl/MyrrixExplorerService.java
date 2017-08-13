package uk.co.recipes.service.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IExplorerFilterDef;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static uk.co.recipes.metrics.MetricNames.TIMER_ITEMS_MOSTSIMILAR;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_MOSTSIMILAR;

/**
 * TODO
 *
 * @author andrewregan
 */
@Singleton
public class MyrrixExplorerService implements IExplorerAPI {

    @Inject
    MyrrixTasteSimilarityService tasteSimilarity;
    @Inject
    ClientRecommender recommender;
    @Inject
    EsItemFactory itemsFactory;
    @Inject
    EsRecipeFactory recipesFactory;
    @Inject
    MetricRegistry metrics;
    @Inject
    ObjectMapper mapper;

    @Inject
    public MyrrixExplorerService() {
        // For Dagger
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IExplorerAPI#similarIngredients(uk.co.recipes.api.IUser, int)
     */
    @Override
    public List<ICanonicalItem> similarIngredients(final ICanonicalItem item, int inNumRecs) {
        return similarIngredients(item, ExplorerFilterDefs.nullFilter(), inNumRecs);
    }

    @Override
    public List<ICanonicalItem> similarIngredients(final ICanonicalItem item, final IExplorerFilterDef inFilterDef, final int inNumRecs) {
        try (Context ignored = metrics.timer(TIMER_ITEMS_MOSTSIMILAR).time()) {
//			final HystrixCommand<List<RecommendedItem>> cmd = new MyrrixSimilarItemsCommand( item.getId(), inNumRecs, new String[]{"ITEM", mapper.writeValueAsString(inFilterDef)} );
//			return itemsFactory.getAll( MyrrixUtils.getItems( cmd.execute() ) );
            final MyrrixSimilarItemsCommand cmd = new MyrrixSimilarItemsCommand(item.getId(), inNumRecs, new String[]{"ITEM", mapper.writeValueAsString(inFilterDef)});
            return itemsFactory.getAll(MyrrixUtils.getItems(cmd.run()));
        } catch (IOException | TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IExplorerAPI#similarRecipes(uk.co.recipes.api.IUser, int)
     */
    @Override
    public List<IRecipe> similarRecipes(final IRecipe inTarget, int inNumRecs) {
        return similarRecipes(inTarget, ExplorerFilterDefs.nullFilter(), inNumRecs);
    }

    @Override
    public List<IRecipe> similarRecipes(final IRecipe recipe, final IExplorerFilterDef inFilterDef, final int inNumRecs) {
        try (Context ignored = metrics.timer(TIMER_RECIPES_MOSTSIMILAR).time()) {
//			final HystrixCommand<List<RecommendedItem>> cmd = new MyrrixSimilarItemsCommand( recipe.getId(), inNumRecs, new String[]{"RECIPE", mapper.writeValueAsString(inFilterDef)} );
//			return recipesFactory.getAll( MyrrixUtils.getItems( cmd.execute() ) );
            final MyrrixSimilarItemsCommand cmd = new MyrrixSimilarItemsCommand(recipe.getId(), inNumRecs, new String[]{"RECIPE", mapper.writeValueAsString(inFilterDef)});
            return recipesFactory.getAll(MyrrixUtils.getItems(cmd.run()));
        } catch (IOException | TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarIngredients(long, int)
     */
    @Override
    public List<Long> similarIngredients(final long inUser, int inNumRecs) {
        return tasteSimilarity.similarIngredients(inUser, inNumRecs);
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarRecipes(long, int)
     */
    @Override
    public List<Long> similarRecipes(final long inUser, int inNumRecs) {
        return tasteSimilarity.similarRecipes(inUser, inNumRecs);
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IExplorerAPI#similarity(uk.co.recipes.api.ICanonicalItem, uk.co.recipes.api.ICanonicalItem)
     */
    @Override
    public float similarity(final ICanonicalItem item1, final ICanonicalItem item2) {
        return similarityToItem(item1.getId(), item2.getId());
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.IExplorerAPI#similarity(uk.co.recipes.api.IRecipe, uk.co.recipes.api.IRecipe)
     */
    @Override
    public float similarity(final IRecipe recipe1, final IRecipe recipe2) {
        return similarityToItem(recipe1.getId(), recipe2.getId());
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarityToItem(long, long)
     */
    @Override
    public float similarityToItem(final long item1, final long item2) {
        return tasteSimilarity.similarityToItem(item1, item2);
    }

    private class MyrrixSimilarItemsCommand /* extends HystrixCommand<List<RecommendedItem>> */ {

        private long[] itemIds;
        private int howMany;
        private String[] rescorerParams;

        public MyrrixSimilarItemsCommand(long inItemId, int howMany, String[] rescorerParams) {
            // super( HystrixCommandGroupKey.Factory.asKey("Explorer.Similarity") );
            this.itemIds = new long[]{inItemId};
            this.howMany = howMany;
            this.rescorerParams = rescorerParams;
        }

        // @Override
        protected List<RecommendedItem> run() throws TasteException {
            return recommender.mostSimilarItems(this.itemIds, this.howMany, this.rescorerParams, /* "contextUserID" */ 0L);
        }
    }
}