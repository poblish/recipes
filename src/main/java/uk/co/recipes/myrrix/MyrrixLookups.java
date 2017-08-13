package uk.co.recipes.myrrix;

import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MyrrixLookups {

    private static final Logger LOG = LoggerFactory.getLogger(MyrrixLookups.class);

    @Inject ClientRecommender recommender;

    @Inject
    public MyrrixLookups() {
        // For Dagger
    }

    public List<Long> recommend(long inUser, int inNumRecs, boolean b, String[] strings) throws TasteException {
        return getItems( recommender.recommend(inUser, inNumRecs, b, strings) );
    }

    public List<Long> recommendToAnonymous(long[] itemIDs, float[] values, int howMany, String[] rescorerParams, Long contextUserID) throws TasteException {
        return getItems( recommender.recommendToAnonymous(itemIDs, values, howMany, rescorerParams, contextUserID) );
    }

    public List<Long> mostSimilarItems(long[] itemIDs, int howMany, String[] rescorerParams, Long contextUserID) throws TasteException {
        return getItems( recommender.mostSimilarItems(itemIDs, howMany, rescorerParams, contextUserID) );
    }

    public float similarityToItem(long itemOrRecipe1, long itemOrRecipe2) throws TasteException {
        return recommender.similarityToItem(itemOrRecipe1, itemOrRecipe2)[0];
    }

    private static List<Long> getItems(final List<RecommendedItem> inItems) {
        return inItems.stream().map(RecommendedItem::getItemID).collect(toList());
    }
}