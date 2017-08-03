package uk.co.recipes.myrrix;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * TODO
 *
 * @author andrewregan
 */
public final class MyrrixUtils {

    private MyrrixUtils() {
    }

    public static List<Long> getItems(final List<RecommendedItem> inItems) {
        return inItems.stream().map(RecommendedItem::getItemID).collect(toList());
    }
}
