/**
 * 
 */
package uk.co.recipes.service.taste.api;

import java.util.List;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ITasteSimilarityAPI {

	List<Long> similarIngredients( final long inUser, final int inNumRecs);
	List<Long> similarRecipes( final long inUser, final int inNumRecs);

	float similarityToItem( final long item1, final long item2);
}
