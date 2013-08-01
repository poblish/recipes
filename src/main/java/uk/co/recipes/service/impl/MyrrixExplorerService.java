/**
 * 
 */
package uk.co.recipes.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.common.base.Throwables;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.myrrix.MyrrixUtils;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixExplorerService implements IExplorerAPI {

	@Inject
	MyrrixTasteSimilarityService tasteSimilarity;

	@Inject
	ClientRecommender recommender;

	@Inject
	EsItemFactory itemsFactory;

	@Inject
	EsRecipeFactory recipesFactory;


	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IExplorerAPI#similarIngredients(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<ICanonicalItem> similarIngredients( final ICanonicalItem inTarget, int inNumRecs) {
		try {
			return itemsFactory.getAll( MyrrixUtils.getItems( recommender.mostSimilarItems( new long[]{ inTarget.getId() }, inNumRecs, new String[]{"ITEM"}, /* "contextUserID" */ 0L) ) );
		}
		catch (NoSuchItemException e) {
			return Collections.emptyList();
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
        catch (IOException e) {
            throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
        }
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.IExplorerAPI#similarRecipes(uk.co.recipes.api.IUser, int)
	 */
	@Override
	public List<IRecipe> similarRecipes( final IRecipe inTarget, int inNumRecs) {
		try {
			return recipesFactory.getAll( MyrrixUtils.getItems( recommender.mostSimilarItems( new long[]{ inTarget.getId() }, inNumRecs, new String[]{"RECIPE"}, /* "contextUserID" */ 0L) ) );
		}
		catch (NoSuchItemException e) {
			return Collections.emptyList();
		}
		catch (TasteException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
		catch (IOException e) {
			throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarIngredients(long, int)
	 */
	@Override
	public List<Long> similarIngredients( final long inUser, int inNumRecs) {
		return tasteSimilarity.similarIngredients( inUser, inNumRecs);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.taste.api.ITasteSimilarityAPI#similarRecipes(long, int)
	 */
	@Override
	public List<Long> similarRecipes( final long inUser, int inNumRecs) {
		return tasteSimilarity.similarRecipes( inUser, inNumRecs);
	}
}