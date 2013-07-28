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

	long getId();
	void setId( long id);

	String getTitle();

	Collection<IIngredient> getIngredients();

	Collection<ICanonicalItem> getItems();

	List<IRecipeStage> getStages();
}
