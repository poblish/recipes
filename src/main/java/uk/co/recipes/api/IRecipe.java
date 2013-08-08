/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
	Locale getLocale();

	Collection<IIngredient> getIngredients();

	Collection<ICanonicalItem> getItems();

	List<IRecipeStage> getStages();
}
