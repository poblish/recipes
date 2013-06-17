/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;
import java.util.List;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IRecipe extends ITagging {

	Collection<IIngredient> ingredients();

	List<IRecipeStage> stages();
}
