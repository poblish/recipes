/**
 * 
 */
package uk.co.recipes.ratings;

import com.google.common.base.Objects;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.ratings.IRecipeRating;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class RecipeRating implements IRecipeRating {

    private final IUser rater;
    private final IRecipe target;
    private final int score;

    public RecipeRating( final IUser inRater, final IRecipe inTarget, int inScore) {
        rater = checkNotNull(inRater);
        target = checkNotNull(inTarget);
        score = inScore;
    }

    @Override
    public IUser getRater() {
        return rater;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public IRecipe getRecipe() {
        return target;
    }

    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                        .add( "rater", rater)
                        .add( "recipe", target)
                        .add( "score", score)
                        .toString();
    }
}
