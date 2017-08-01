/**
 * 
 */
package uk.co.recipes.ratings;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ratings.IItemRating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class ItemRating implements IItemRating {

//    private final IUser rater;
    private final ICanonicalItem target;
    private final int score;

//    public ItemRating( final IUser inRater, final ICanonicalItem inTarget, int inScore) {
//        rater = checkNotNull(inRater);
//        target = checkNotNull(inTarget);
//        score = inScore;
//    }

    @JsonCreator
    public ItemRating( @JsonProperty("item") final ICanonicalItem inTarget, @JsonProperty("score") int inScore) {
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
    public ICanonicalItem getItem() {
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
        if (!(obj instanceof ItemRating)) {
            return false;
        }
        final ItemRating other = (ItemRating) obj;
        // No user? Hmm...
        return score == other.score && Objects.equal( target, other.target);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
//                        .add( "rater", rater)
                        .add( "item", target)
                        .add( "score", score)
                        .toString();
    }
}
