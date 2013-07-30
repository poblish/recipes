/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixUpdater implements IEventListener {

    @Inject
    IEventService eventService;

    // Yuk: why can't Dagger do @PostConstruct ?
    public void startListening() {
        checkNotNull(eventService).addListener(this);
    }

    @Override
    public void onAddItem( final ItemEvent evt) {
        System.out.println("# Add: " + evt);
        // throw new RuntimeException("unimpl");
    }

    @Override
    public void onRateItem( final ItemEvent evt) {
        System.out.println("# Rate: " + evt);
        // throw new RuntimeException("unimpl");
    }
}
