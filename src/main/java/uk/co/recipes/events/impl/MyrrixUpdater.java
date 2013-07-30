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

        final long itemId = evt.getItem().getId();
        boolean changesMade = false;

        try {
        	for ( final Entry<ITag,Serializable> eachTag : evt.getItem().getTags().entrySet()) {
        		if ( eachTag.getValue() instanceof Boolean) {
        			if ((Boolean) eachTag.getValue()) {
        				// Don't bother setting if == FALSE
        	        	recommender.setItemTag( eachTag.getKey().toString(), itemId, 1.0f);
        	        	changesMade = true;
        			}
        		}
        		else {
        			final float val = Float.valueOf((String) eachTag.getValue());
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