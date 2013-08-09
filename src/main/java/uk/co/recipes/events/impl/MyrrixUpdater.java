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
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.DefaultIngredientQuantityScoreBooster;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixUpdater implements IEventListener {

	private static final Logger LOG = LoggerFactory.getLogger( MyrrixUpdater.class );

    @Inject
    IEventService eventService;

    @Inject
    EsItemFactory itemFactory;  // Purely for getting Item Ids

    @Inject
    ClientRecommender recommender;

    @Inject
    // FIXME FIXME - Why doesn't injection work here?
    IIngredientQuantityScoreBooster booster = new DefaultIngredientQuantityScoreBooster();


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
            changesMade = addItemTagsForItem( evt.getItem(), itemId, 1.0f);
        }
        catch (TasteException e) {
            Throwables.propagate(e);
        }

        if (changesMade) {
            recommender.refresh();

            if (LOG.isTraceEnabled()) {
                LOG.trace("onAddItem: refresh done");
            }
        }
    }

    @Override
    public void onDeleteItem( final ItemEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteItem: " + evt);
        }

        final long itemId = evt.getItem().getId();
        boolean changesMade = false;

        try {
            changesMade = removeItemTagsForItem( evt.getItem(), itemId);
        }
        catch (TasteException e) {
            Throwables.propagate(e);
        }

        if (changesMade) {
            recommender.refresh();

            if (LOG.isTraceEnabled()) {
                LOG.trace("onDeleteItem: refresh done");
            }
        }
    }

    @Override
    public void onAddRecipe( final RecipeEvent evt) {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onAddRecipe: " + evt);
    	}

        final long recipeId = evt.getRecipe().getId();
        boolean changesMade = false;

        try {
            for ( IIngredient eachIngr : evt.getRecipe().getIngredients()) {
            	changesMade |= addItemTagsForItem( eachIngr.getItem(), recipeId, 1.0f * booster.getBoostForQuantity( eachIngr.getItem(), eachIngr.getQuantity()));
        	}
	    }
		catch (TasteException e) {
			Throwables.propagate(e);
		}

        if (changesMade) {
        	recommender.refresh();

        	if (LOG.isTraceEnabled()) {
        		LOG.trace("onAddRecipe: refresh done");
        	}
        }
    }

    @Override
    public void onDeleteRecipe(RecipeEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteRecipe: " + evt);
        }

        final long recipeId = evt.getRecipe().getId();
        boolean changesMade = false;

        try {
            for ( IIngredient eachIngr : evt.getRecipe().getIngredients()) {
                changesMade |= removeItemTagsForItem( eachIngr.getItem(), recipeId);
            }
        }
        catch (TasteException e) {
            Throwables.propagate(e);
        }

        if (changesMade) {
            recommender.refresh();

            if (LOG.isTraceEnabled()) {
                LOG.trace("onDeleteRecipe: refresh done");
            }
        }
    }

    private boolean addItemTagsForItem( final ICanonicalItem inItem, final long inItemOrRecipeId, final float inBasicScore) throws TasteException {
    	boolean changesMade = addHierarchicalSimilarityTags( inItem, inItemOrRecipeId, inBasicScore);
  
    	for ( final Entry<ITag,Serializable> eachTag : inItem.getTags().entrySet()) {
    		if ( eachTag.getValue() instanceof Boolean) {
    			if ((Boolean) eachTag.getValue()) {
    				// Don't bother setting if == FALSE
    				if (LOG.isDebugEnabled()) {
    					LOG.debug("Set Tag '" + eachTag.getKey() + "' val=1.0 for " + inItemOrRecipeId);
    				}
    	        	recommender.setItemTag( eachTag.getKey().toString(), inItemOrRecipeId, inBasicScore);
    	        	changesMade = true;
    			}
    		}
    		else {
    			final float val = Float.valueOf((String) eachTag.getValue());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Set Tag '" + eachTag.getKey() + "' val=" + val + " for " + inItemOrRecipeId);
				}
	        	recommender.setItemTag( eachTag.getKey().toString(), inItemOrRecipeId, val);
	        	changesMade = true;
    		}
    	}
 
    	return changesMade;
    }

    private boolean addHierarchicalSimilarityTags( final ICanonicalItem inItem, final long inItemOrRecipeId, final float inScore) throws TasteException {
    	boolean changesMade = false;

    	final String ourPseudoParentTagName = "PARENT_" + itemFactory.toStringId(inItem);
 
    	if (LOG.isDebugEnabled()) {
    	    LOG.debug("Set Tag '" + ourPseudoParentTagName + "' val=" + inScore + " for " + inItemOrRecipeId);
		}
    	recommender.setItemTag( ourPseudoParentTagName, inItemOrRecipeId, inScore);

    	if ( inItem.parent().isPresent()) {
    		changesMade |= addHierarchicalSimilarityTags( inItem.parent().get(), inItemOrRecipeId, inScore * 0.8f);
    	}
  
    	return changesMade;
    }

    private boolean removeItemTagsForItem( final ICanonicalItem inItem, final long inItemOrRecipeId) throws TasteException {
        LOG.debug("removeItemTagsForItem: " + inItemOrRecipeId + " for " + inItemOrRecipeId);
        return false;
    }

    @Override
    public void onRateItem( final ItemEvent evt) {
    	checkArgument( evt.getItem().getId() >= 0 && evt.getItem().getId() < Recipe.BASE_ID, "Item has not been persisted, or Id is invalid");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Rate: " + evt);
		}

        rateGenericItem( evt.getUser().getId(), evt.getItem().getId());
    }

    @Override
    public void onRateRecipe( final RecipeEvent evt) {
    	checkArgument( evt.getRecipe().getId() >= Recipe.BASE_ID, "Recipe has not been persisted");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Rate: " + evt);
		}

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