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
public interface ITasteRecommendationsAPI {

	List<Long> recommendIngredients( final long inUser, final int inNumRecs);
	List<Long> recommendRecipes( final long inUser, final int inNumRecs);
}
