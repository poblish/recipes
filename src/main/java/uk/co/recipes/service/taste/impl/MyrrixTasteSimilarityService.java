/**
 * 
 */
package uk.co.recipes.service.taste.impl;

import java.util.List;

import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.common.base.Throwables;

import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.service.taste.api.ITasteSimilarityAPI;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixTasteSimilarityService implements ITasteSimilarityAPI {

	@Inject
	ClientRecommender recommender;

	@Override
	public List<Long> similarIngredients( long inUser, int inNumRecs) {
		try {
			return MyrrixUtils.getItems( recommender.mostSimilarItems( inUser, inNumRecs) );
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	@Override
	public List<Long> similarRecipes( long inUser, int inNumRecs) {
		throw new RuntimeException("unimpl");
	}		
}