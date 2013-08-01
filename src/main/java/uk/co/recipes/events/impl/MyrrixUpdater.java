/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixUpdater implements IEventListener {

	private final static Logger LOG = LoggerFactory.getLogger( MyrrixUpdater.class );

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
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onAddItem: " + evt);
    	}

        final long itemId = evt.getItem().getId();
        boolean changesMade = false;

        try {
        	for ( final Entry<ITag,Serializable> eachTag : evt.getItem().getTags().entrySet()) {
        		if ( eachTag.getValue() instanceof Boolean) {
        			if ((Boolean) eachTag.getValue()) {
        				// Don't bother setting if == FALSE
        				if (LOG.isDebugEnabled()) {
        					LOG.debug("Set Tag '" + eachTag.getKey() + "' val=1.0 for " + itemId);
        				}
        	        	recommender.setItemTag( eachTag.getKey().toString(), itemId, 1.0f);
        	        	changesMade = true;
        			}
        		}
        		else {
        			final float val = Float.valueOf((String) eachTag.getValue());
    				if (LOG.isDebugEnabled()) {
    					LOG.debug("Set Tag '" + eachTag.getKey() + "' val=" + val + " for " + itemId);
    				}
    	        	recommender.setItemTag( eachTag.getKey().toString(), itemId, val);
    	        	changesMade = true;
        		}
        	}
	    }
		catch (TasteException e) {
			Throwables.propagate(e);
		}

        if (changesMade) {
        	recommender.refresh();
//            System.out.println("# Changes saved!");
        }
    }

    @Override
    public void onRateItem( final ItemEvent evt) {
    	checkArgument( evt.getItem().getId() >= 0 && evt.getItem().getId() < Recipe.BASE_ID, "Item has not been persisted, or Id is invalid");

        System.out.println("# Rate: " + evt);
        rateGenericItem( evt.getUser().getId(), evt.getItem().getId());
    }

    @Override
    public void onRateRecipe( final RecipeEvent evt) {
    	checkArgument( evt.getRecipe().getId() >= Recipe.BASE_ID, "Recipe has not been persisted");

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