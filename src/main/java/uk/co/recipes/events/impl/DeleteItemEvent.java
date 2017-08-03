/**
 *
 */
package uk.co.recipes.events.impl;

import uk.co.recipes.api.ICanonicalItem;


/**
 * TODO
 *
 * @author andrewr
 */
public class DeleteItemEvent extends AbstractItemEvent {

    private static final long serialVersionUID = 1L;

    public DeleteItemEvent(final ICanonicalItem inItem) {
        super(null, inItem, 1.0f);
    }
}