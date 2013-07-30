/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

import com.google.common.base.Objects;


/**
 * @author andrewr
 *
 */
public class RecipeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IUser user;
    private final IRecipe recipe;
    private final float rating;

    public RecipeEvent(IUser user, IRecipe inItem, float inRating) {
        this.user = user;
        this.recipe = checkNotNull(inItem);
        this.rating = inRating;
    }

	public IUser getUser() {
		return user;
	}

	public IRecipe getRecipe() {
		return recipe;
	}

	public float getRating() {
		return rating;
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "user", user)
						.add( "recipe", recipe)
						.add( "rating", rating)
						.toString();
	}
}