/**
 * 
 */
package uk.co.recipes.events.impl;

import java.io.Serializable;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author andrewr
 *
 */
public class ItemEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IUser user;
    private final ICanonicalItem item;

    public ItemEvent(IUser user, ICanonicalItem inItem) {
        this.user = user;
        this.item = checkNotNull(inItem);
    }
}