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
public class RateRecipeEvent extends AbstractRecipeEvent {

    private static final long serialVersionUID = 1L;

    public RateRecipeEvent( final IUser user, final IRecipe inItem, final float inRating) {
        super(user, inItem, inRating);
    }
}