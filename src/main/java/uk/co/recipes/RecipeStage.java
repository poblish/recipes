/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipeStage;

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
}