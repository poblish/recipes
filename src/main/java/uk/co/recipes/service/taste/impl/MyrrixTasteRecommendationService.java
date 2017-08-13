package uk.co.recipes.service.taste.impl;

import org.apache.mahout.cf.taste.common.TasteException;
import uk.co.recipes.myrrix.MyrrixLookups;
import uk.co.recipes.service.taste.api.ITasteRecommendationsAPI;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MyrrixTasteRecommendationService implements ITasteRecommendationsAPI {

    @Inject MyrrixLookups recommender;

    @Inject
    public MyrrixTasteRecommendationService() {
        // For Dagger
    }

    @Override
    public List<Long> recommendIngredients(long inUser, int inNumRecs) {
        try {
            return recommender.recommend(inUser, inNumRecs, false, new String[]{"ITEM"});
        } catch (TasteException e) {
            throw new RuntimeException(e);  // Yuk, FIXME, let's get the API right
        }
    }

    @Override
    public List<Long> recommendRecipes(long inUser, int inNumRecs) {
        throw new RuntimeException("unimpl");
    }
}