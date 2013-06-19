/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipeStage;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeStage implements IRecipeStage {

	private final Collection<IIngredient> ingredients = Lists.newArrayList();

	public void addIngredient( final IIngredient ingredient) {
		ingredients.add(ingredient);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "ingredients", ingredients)
						.toString();
	}
}