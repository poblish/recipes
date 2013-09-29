/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

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

	IUser getCreator();
	DateTime getCreationTime();

	Object clone();

	IForkDetails getForkDetails();
	void setForkDetails( final IForkDetails inForkDetails);

	Collection<IIngredient> getIngredients();
	Collection<IIngredient> getSortedIngredients();

	Collection<ICanonicalItem> getItems();
	boolean containsItem( final ICanonicalItem item);

	List<IRecipeStage> getStages();

	boolean removeItems( final ICanonicalItem... inItems);
	boolean addIngredients( final IIngredient... inIngredients);
	boolean removeIngredients( final IIngredient... inIngredients);
}
