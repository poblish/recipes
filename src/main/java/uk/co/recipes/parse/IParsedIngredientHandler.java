/**
 *
 */
package uk.co.recipes.parse;

import uk.co.recipes.api.IIngredient;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IParsedIngredientHandler {

    void foundIngredient(IIngredient ingr);
}
