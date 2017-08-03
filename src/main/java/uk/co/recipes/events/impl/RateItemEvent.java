/**
 *
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;


/**
 * TODO
 *
 * @author andrewr
 */
public class RateItemEvent extends AbstractItemEvent {

    private static final long serialVersionUID = 1L;

    public RateItemEvent(final IUser user, final ICanonicalItem inItem, final float inScore) {
        super(user, inItem, inScore);
    }
}