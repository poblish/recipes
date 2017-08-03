/**
 *
 */
package uk.co.recipes.service.taste.impl;

import com.google.common.base.Throwables;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.service.taste.api.ITasteSimilarityAPI;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author andrewregan
 */
@Singleton
public class MyrrixTasteSimilarityService implements ITasteSimilarityAPI {

    @Inject
    ClientRecommender recommender;

    @Inject
    public MyrrixTasteSimilarityService() {
        // For Dagger
    }

    @Override
    public List<Long> similarIngredients(long inUser, int inNumRecs) {
        try {
            return MyrrixUtils.getItems(recommender.mostSimilarItems(new long[]{inUser}, inNumRecs, new String[]{"ITEM"}, /* "contextUserID" */ 0L));
        } catch (NoSuchItemException e) {
            return Collections.emptyList();
        } catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
    }

    @Override
    public List<Long> similarRecipes(long inUser, int inNumRecs) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public float similarityToItem(long itemOrRecipe1, long itemOrRecipe2) {
        try {
            return recommender.similarityToItem(itemOrRecipe1, itemOrRecipe2)[0];
        } catch (TasteException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
    }
}