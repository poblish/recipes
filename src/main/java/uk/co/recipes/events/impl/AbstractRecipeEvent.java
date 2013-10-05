/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

import com.google.common.base.Objects;


/**
 * @author andrewr
 *
 */
public abstract class AbstractRecipeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IUser user;
    private final IRecipe recipe;
    private final float score;

    public AbstractRecipeEvent(IUser user, IRecipe inRecipe, float inScore) {
        this.user = user;
        this.recipe = checkNotNull( inRecipe, "Recipe cannot be null");
        this.score = inScore;

        checkArgument( inRecipe.getId() >= Recipe.BASE_ID, "Recipe has not been persisted");
    }

	public IUser getUser() {
		return user;
	}

	public IRecipe getRecipe() {
		return recipe;
	}

	public float getScore() {
		return score;
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "user", user)
						.add( "recipe", recipe.getId())
						.add( "score", score)
						.toString();
	}
}