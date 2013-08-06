/**
 * 
 */
package uk.co.recipes.api.ratings;

import uk.co.recipes.api.IRecipe;


/**
 * @author andrewr
 *
 */
public interface IRecipeRating extends IRating {

    IRecipe getRecipe();
}
