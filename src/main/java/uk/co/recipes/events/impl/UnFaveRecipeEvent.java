/**
 * 
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class UnFaveRecipeEvent extends AbstractRecipeEvent {

    private static final long serialVersionUID = 1L;

    public UnFaveRecipeEvent( final IUser user, final IRecipe inItem) {
        super(user, inItem, -10.0f);
    }
}