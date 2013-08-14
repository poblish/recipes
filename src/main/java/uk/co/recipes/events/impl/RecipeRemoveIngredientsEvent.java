/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;

import com.google.common.base.Objects;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeRemoveIngredientsEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final IRecipe recipe;
    private final IIngredient item;

    public RecipeRemoveIngredientsEvent( final IRecipe inRecipe, final IIngredient inItem) {
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
		return Objects.toStringHelper(this).omitNullValues()
						.add( "recipe", recipe.getId())
						.add( "ingredient", item)
						.toString();
	}
}
