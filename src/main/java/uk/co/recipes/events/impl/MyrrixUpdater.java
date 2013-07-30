/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;

import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.common.base.Throwables;

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

    @Inject
    ClientRecommender recommender;

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
        rateGenericItem( evt.getUser().getId(), evt.getItem().getId());
    }

    @Override
    public void onRateRecipe( final RecipeEvent evt) {
        System.out.println("# Rate: " + evt);
        rateGenericItem( evt.getUser().getId(), evt.getRecipe().getId());
    }

    private void rateGenericItem( final long inUserId, final long inGenericItemId) {
		try {
			recommender.ingest( new StringReader( inUserId + "," + inGenericItemId) );
			recommender.refresh();
		}
		catch (TasteException e) {
			Throwables.propagate(e);
		}
    }
}