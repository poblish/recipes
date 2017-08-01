/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;

import com.google.common.base.Objects;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeAddIngredientsEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final IRecipe recipe;
    private final IIngredient item;

    public RecipeAddIngredientsEvent( final IRecipe inRecipe, final IIngredient inItem) {
    	recipe = checkNotNull(inRecipe);
    	item = checkNotNull(inItem);
    }

	public IRecipe getRecipe() {
		return recipe;
	}

	public IIngredient getIngredient() {
		return item;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
						.add( "recipe", recipe.getId())
						.add( "ingredient", item)
						.toString();
	}
}
