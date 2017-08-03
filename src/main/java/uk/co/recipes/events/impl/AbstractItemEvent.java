/**
 *
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.google.common.base.MoreObjects;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;


/**
 * @author andrewr
 *
 */
public abstract class AbstractItemEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IUser user;
    private final ICanonicalItem item;
    private final float score;

    public AbstractItemEvent(IUser user, ICanonicalItem inItem, float inScore) {
        this.user = user;
        this.item = checkNotNull(inItem);
        this.score = inScore;

    	checkArgument( inItem.getId() >= 0 && inItem.getId() < Recipe.BASE_ID, "Item has not been persisted, or Id is invalid");
    }

	public IUser getUser() {
		return user;
	}

	public ICanonicalItem getItem() {
		return item;
	}

	public float getScore() {
		return score;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
						.add( "user", user)
						.add( "item", item.getId())
						.add( "score", score)
						.toString();
	}
}