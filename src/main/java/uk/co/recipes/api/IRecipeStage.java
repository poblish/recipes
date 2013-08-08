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

    boolean removeItems( final ICanonicalItem... inItems);
}
