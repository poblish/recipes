/**
 *
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

/**
 * TODO
 *
 * @author andrewregan
 */
public class RecipeVisitedEvent extends AbstractRecipeEvent {

    private static final long serialVersionUID = 1L;

    public RecipeVisitedEvent(final IUser user, final IRecipe inRecipe) {
        super(user, inRecipe, 1.0f);
    }
}
