/**
 * 
 */
package uk.co.recipes.ratings;

import com.google.common.base.Objects;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ratings.IRecipeRating;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class RecipeRating implements IRecipeRating {

//    private final IUser rater;
    private final IRecipe target;
    private final int score;

//    public RecipeRating( final IUser inRater, final IRecipe inTarget, int inScore) {
//        rater = checkNotNull(inRater);
//        target = checkNotNull(inTarget);
//        score = inScore;
//    }

    @JsonCreator
    public RecipeRating( @JsonProperty("recipe") final IRecipe inTarget, @JsonProperty("score") int inScore) {
//        rater = null;  // FIXME
        target = checkNotNull(inTarget);
        score = inScore;
    }

//    @JsonIgnore
//    @Override
//    public IUser getRater() {
//        return rater;
//    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public IRecipe getRecipe() {
        return target;
    }

    @Override
    public int hashCode() {
        // No user? Hmm...
        return Objects.hashCode( target, score);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RecipeRating)) {
            return false;
        }
        final RecipeRating other = (RecipeRating) obj;
        // No user? Hmm...
        return score == other.score && Objects.equal( target, other.target);
    }

    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
//                        .add( "rater", rater)
                        .add( "recipe", target)
                        .add( "score", score)
                        .toString();
    }
}
