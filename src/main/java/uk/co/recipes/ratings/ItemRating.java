/**
 * 
 */
package uk.co.recipes.ratings;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Objects;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.ratings.IItemRating;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class ItemRating implements IItemRating {

    private final IUser rater;
    private final ICanonicalItem target;
    private final int score;

    public ItemRating( final IUser inRater, final ICanonicalItem inTarget, int inScore) {
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
    public ICanonicalItem getItem() {
        return target;
    }

    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                        .add( "rater", rater)
                        .add( "item", target)
                        .add( "score", score)
                        .toString();
    }
}
