package uk.co.recipes.service.taste.impl;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import uk.co.recipes.myrrix.MyrrixLookups;
import uk.co.recipes.service.taste.api.ITasteSimilarityAPI;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class MyrrixTasteSimilarityService implements ITasteSimilarityAPI {

    @Inject MyrrixLookups recommender;

    @Inject
    public MyrrixTasteSimilarityService() {
        // For Dagger
    }

    @Override
    public List<Long> similarIngredients(long inUser, int inNumRecs) {
        try {
            return recommender.mostSimilarItems(new long[]{inUser}, inNumRecs, new String[]{"ITEM"}, /* "contextUserID" */ 0L);
        } catch (NoSuchItemException e) {
            return Collections.emptyList();
        } catch (TasteException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        }
    }

    @Override
    public List<Long> similarRecipes(long inUser, int inNumRecs) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public float similarityToItem(long itemOrRecipe1, long itemOrRecipe2) {
        try {
            return recommender.similarityToItem(itemOrRecipe1, itemOrRecipe2);
        } catch (TasteException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        }
    }
}