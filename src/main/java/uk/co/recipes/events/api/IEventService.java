/**
 * 
 */
package uk.co.recipes.events.api;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IEventService {

    void addListener( final IEventListener inL);

    void addItem( final ICanonicalItem inItem);
    void rateItem( final IUser inUser, final ICanonicalItem inItem);
}