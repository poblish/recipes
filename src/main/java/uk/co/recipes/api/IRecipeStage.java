/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IRecipeStage {

	Collection<IIngredient> getIngredients();
    Collection<ICanonicalItem> getItems();

	boolean containsItem( final ICanonicalItem item);

    boolean removeItems( final ICanonicalItem... inItems);
	boolean addIngredients( final IIngredient... inIngredients);
	boolean removeIngredients( final IIngredient... inIngredients);
}
