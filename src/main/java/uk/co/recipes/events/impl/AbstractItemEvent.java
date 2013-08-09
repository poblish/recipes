/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;

import com.google.common.base.Objects;


/**
 * @author andrewr
 *
 */
public abstract class AbstractItemEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IUser user;
    private final ICanonicalItem item;
    private final float rating;

    public AbstractItemEvent(IUser user, ICanonicalItem inItem, float inRating) {
        this.user = user;
        this.item = checkNotNull(inItem);
        this.rating = inRating;
    }

	public IUser getUser() {
		return user;
	}

	public ICanonicalItem getItem() {
		return item;
	}

	public float getRating() {
		return rating;
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "user", user)
						.add( "item", item.getId())
						.add( "rating", rating)
						.toString();
	}
}