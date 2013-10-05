/**
 * 
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ItemVisitedEvent extends AbstractItemEvent {

    private static final long serialVersionUID = 1L;

    public ItemVisitedEvent( final IUser user, final ICanonicalItem inItem) {
        super(user, inItem, 1.0f);
    }
}
