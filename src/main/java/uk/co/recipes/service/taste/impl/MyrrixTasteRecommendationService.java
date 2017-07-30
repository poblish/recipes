/**
 * 
 */
package uk.co.recipes.service.taste.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;

import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.service.taste.api.ITasteRecommendationsAPI;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
@Singleton
public class MyrrixTasteRecommendationService implements ITasteRecommendationsAPI {

	@Inject
	ClientRecommender recommender;

	@Override
	public List<Long> recommendIngredients( long inUser, int inNumRecs) {
		try {
			return MyrrixUtils.getItems( recommender.recommend( inUser, inNumRecs, false, new String[]{"ITEM"}) );
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	@Override
	public List<Long> recommendRecipes( long inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}		
}