/**
 *
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.IRecipe;


/**
 * TODO
 *
 * @author andrewr
 */
public class AddRecipeEvent extends AbstractRecipeEvent {

    private static final long serialVersionUID = 1L;

    public AddRecipeEvent(final IRecipe inItem) {
        super(null, inItem, 1.0f);
    }
}