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
public interface IRecipe extends ITagging, Cloneable, java.io.Serializable {

	long getId();
	void setId( long id);

	String getTitle();
	void setTitle( String title);

	Locale getLocale();

	Object clone();

	IForkDetails getForkDetails();
	void setForkDetails( final IForkDetails inForkDetails);

	Collection<IIngredient> getIngredients();

	Collection<ICanonicalItem> getItems();

	List<IRecipeStage> getStages();

	boolean removeItems( final ICanonicalItem... inItems);
}
