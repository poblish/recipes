/**
 * 
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.ICanonicalItem;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class AddItemEvent extends AbstractItemEvent {

    private static final long serialVersionUID = 1L;

    public AddItemEvent( final ICanonicalItem inItem) {
        super(null, inItem, 1.0f);
    }
}
