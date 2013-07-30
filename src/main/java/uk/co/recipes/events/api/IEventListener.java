/**
 * 
 */
package uk.co.recipes.events.api;

import uk.co.recipes.events.impl.ItemEvent;


/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IEventListener {

    void onAddItem( final ItemEvent evt);
    void onRateItem( final ItemEvent evt);
}