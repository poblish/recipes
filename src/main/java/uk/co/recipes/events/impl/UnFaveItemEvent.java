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
public class UnFaveItemEvent extends AbstractItemEvent {

    private static final long serialVersionUID = 1L;

    public UnFaveItemEvent(final IUser user, final ICanonicalItem inItem) {
        super(user, inItem, -10.0f);
    }
}