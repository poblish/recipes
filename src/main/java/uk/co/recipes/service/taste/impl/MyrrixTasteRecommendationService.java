/**
 * 
 */
package uk.co.recipes.service.taste.impl;

import java.util.List;

import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.elasticsearch.common.base.Throwables;

import uk.co.recipes.service.taste.api.ITasteRecommendationsAPI;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixTasteRecommendationService implements ITasteRecommendationsAPI {

	@Inject
	ClientRecommender recommender;

	@Override
	public List<Long> recommendIngredients( long inUser, int inNumRecs) {
		try {
			return getRecommendationUsers( recommender.recommend( inUser, inNumRecs) );
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	private static List<Long> getRecommendationUsers( final List<RecommendedItem> inItems) {
		System.out.println(inItems);
		return FluentIterable.from(inItems).transform( new Function<RecommendedItem,Long>() {

			@Override
			public Long apply( RecommendedItem input) {
				return input.getItemID();
			}
		} ).toList();
	}

	@Override
	public List<Long> recommendRecipes( long inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}		
}