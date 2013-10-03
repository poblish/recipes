/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

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

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Floats;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
@Singleton
public class MyrrixUpdater implements IEventListener {

    private static final Logger LOG = LoggerFactory.getLogger( MyrrixUpdater.class );

    @Inject
    IEventService eventService;

    @Inject
    EsItemFactory itemFactory;  // Purely for getting Item Ids

    @Inject
    ClientRecommender recommender;

    @Inject
    IIngredientQuantityScoreBooster booster;


    public MyrrixUpdater() {
    	// For Dagger
    }

    // Yuk: why can't Dagger do @PostConstruct ?
    public void startListening() {
        checkNotNull(eventService).addListener(this);
    }

    @Subscribe
    public void onAddItem( final AddItemEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onAddItem: " + evt);
        }

        final long itemId = evt.getItem().getId();
        boolean changesMade = false;

        try {
            changesMade = setItemTagsForItem( evt.getItem(), itemId, 1.0f);
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

    @Subscribe
    public void onDeleteItem( final DeleteItemEvent evt) {
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

    @Subscribe
    public void onAddRecipe( final AddRecipeEvent evt) {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onAddRecipe: " + evt);
    	}

        addRecipeIngredients( evt.getRecipe().getId(), evt.getRecipe().getLocale(), evt.getRecipe().getIngredients());
    }

    @Subscribe
    public void onDeleteRecipe( final DeleteRecipeEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteRecipe: " + evt);
        }

        removeRecipeIngredients( evt.getRecipe().getId(), evt.getRecipe().getIngredients());
    }

    @Subscribe
    public void onAddRecipeIngredients( final RecipeAddIngredientsEvent evt) {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onAddRecipeIngredients: " + evt);
    	}

        addRecipeIngredients( evt.getRecipe().getId(), evt.getRecipe().getLocale(), Lists.newArrayList( evt.getIngredient() ));
    }

    @Subscribe
    public void onRemoveRecipeIngredients( final RecipeRemoveIngredientsEvent evt) {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onDeleteRecipeIngredients: " + evt);
    	}

        removeRecipeIngredients( evt.getRecipe().getId(), Lists.newArrayList( evt.getIngredient() ));
    }

    public void addRecipeIngredients( final long inRecipeId, final Locale inRecipeLocale, final Collection<IIngredient> inIngredients) {
        boolean changesMade = false;

        try {
            for ( IIngredient eachIngr : inIngredients) {
                final float basicScoreForIngr = getBasicScore(eachIngr);

            	changesMade |= setItemTagsForItem( eachIngr.getItem(), inRecipeId, basicScoreForIngr * booster.getBoostForQuantity( inRecipeLocale, eachIngr.getItem(), eachIngr.getQuantity()));

    			/* FIXME */ recommender.ingest( new StringReader( inRecipeId + "," + eachIngr.getItem().getId() + "," + basicScoreForIngr) );
    			/* FIXME */ changesMade = true;
    		}
	    }
		catch (TasteException e) {
			Throwables.propagate(e);
		}

        if (changesMade) {
        	recommender.refresh();

        	if (LOG.isTraceEnabled()) {
        		LOG.trace("addRecipeIngredients: refresh done");
        	}
        }
    }

    public void removeRecipeIngredients( final long inRecipeId, final Collection<IIngredient> inIngredients) {
        boolean changesMade = false;

        try {
            for ( IIngredient eachIngr : inIngredients) {
                changesMade |= removeItemTagsForItem( eachIngr.getItem(), inRecipeId);

                final float basicScoreForIngr = -getBasicScore(eachIngr);

    			/* FIXME */ recommender.ingest( new StringReader( inRecipeId + "," + eachIngr.getItem().getId() + "," + basicScoreForIngr) );
    			/* FIXME */ changesMade = true;
            }
        }
        catch (TasteException e) {
            Throwables.propagate(e);
        }

        if (changesMade) {
            recommender.refresh();

            if (LOG.isTraceEnabled()) {
                LOG.trace("deleteRecipeIngredients: refresh done");
            }
        }
    }

    @Subscribe
    public void onRemoveRecipeItems( final RecipeRemoveItemsEvent evt) {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("onRemoveRecipeItems: " + evt);
    	}

        boolean changesMade = false;

        try {
            changesMade = removeItemTagsForItem( evt.getItem(), evt.getRecipe().getId());
        }
        catch (TasteException e) {
            Throwables.propagate(e);
        }

        if (changesMade) {
            recommender.refresh();

            if (LOG.isTraceEnabled()) {
                LOG.trace("deleteRecipeIngredients: refresh done");
            }
        }
    }

    private boolean setItemTagsForItem( final ICanonicalItem inItem, final long inItemOrRecipeId, final float inBasicScore) throws TasteException {
    	boolean changesMade = setHierarchicalSimilarityTags( inItem, inItemOrRecipeId, inBasicScore);

    	final String setStr = ( inBasicScore > 0 ? "SET" : "UNSET");

    	for ( final Entry<ITag,Serializable> eachTag : inItem.getTags().entrySet()) {
    		if ( eachTag.getValue() instanceof Boolean) {
    			if ((Boolean) eachTag.getValue()) {
    				// Don't bother setting if == FALSE

    			    final float scoreToUse = eachTag.getKey().getBoost() * inBasicScore;
    			    if (isPointlessScore(scoreToUse)) {
    			        continue;
    			    }

                    changesMade = doSetTag( eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
    			}
    		}
    		else {
    		    final Float fVal = Floats.tryParse((String) eachTag.getValue());
    		    if ( fVal != null) {  // Got a float - treat it as a score
                    final float scoreToUse = /* Think we need the boost...? */ eachTag.getKey().getBoost() * fVal;
                    if (isPointlessScore(scoreToUse)) {
                        continue;
                    }

                    changesMade = doSetTag( eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
    		    }
    		    else {  // It's a String value, e.g. scoville - ignore the value and just use basicScore
                    final float scoreToUse = eachTag.getKey().getBoost() * inBasicScore;
                    if (isPointlessScore(scoreToUse)) {
                        continue;
                    }

                    changesMade = doSetTag( eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
    		    }
    		}
    	}
 
    	return changesMade;
    }

    private boolean doSetTag( final ITag inTag, final String inSetStr, final long inItemOrRecipeId, final float inScoreToUse) throws TasteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug( inSetStr + " Tag '" + inTag + "' val=" + inScoreToUse + " for " + inItemOrRecipeId);
        }
        recommender.setItemTag( inTag.toString(), inItemOrRecipeId, inScoreToUse);
        return true;
    }

    private boolean setHierarchicalSimilarityTags( final ICanonicalItem inItem, final long inItemOrRecipeId, final float inScore) throws TasteException {

        if (isPointlessScore(inScore)) {
            return false;
        }

        boolean changesMade = false;

    	final String ourPseudoParentTagName = "PARENT_" + itemFactory.toStringId(inItem);
 
    	if (LOG.isDebugEnabled()) {
    	    LOG.debug(( inScore > 0 ? "SET" : "UNSET") + " Tag '" + ourPseudoParentTagName + "' val=" + inScore + " for " + inItemOrRecipeId);
		}
    	// recommender.setItemTag( ourPseudoParentTagName, inItemOrRecipeId, inScore);

    	if ( inItem.parent().isPresent()) {
    		changesMade |= setHierarchicalSimilarityTags( inItem.parent().get(), inItemOrRecipeId, inScore * 0.8f);
    	}
  
    	return changesMade;
    }

    private boolean isPointlessScore( final float inScore) {
    	return inScore > -0.01f && inScore < 0.01f;
    }

    private boolean removeItemTagsForItem( final ICanonicalItem inItem, final long inItemOrRecipeId) throws TasteException {
        return setItemTagsForItem( inItem, inItemOrRecipeId, -1.0f);
    }

    @Subscribe
    public void onRateItem( final RateItemEvent evt) {
    	checkArgument( evt.getItem().getId() >= 0 && evt.getItem().getId() < Recipe.BASE_ID, "Item has not been persisted, or Id is invalid");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Rate: " + evt);
		}

        rateGenericItem( evt.getUser().getId(), evt.getItem().getId(), evt.getScore());
    }

    @Subscribe
    public void onRateRecipe( final RateRecipeEvent evt) {
    	checkArgument( evt.getRecipe().getId() >= Recipe.BASE_ID, "Recipe has not been persisted");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Rate: " + evt);
		}

        rateGenericItem( evt.getUser().getId(), evt.getRecipe().getId(), evt.getScore());
    }

    private void rateGenericItem( final long inUserId, final long inGenericItemId, final float inRating) {
		try {
			recommender.ingest( new StringReader( inUserId + "," + inGenericItemId + "," + inRating) );
			recommender.refresh();
		}
		catch (TasteException e) {
			Throwables.propagate(e);
		}
    }

    // For Recipe-based similarity (tags) and recommendations (prefs): de-value optional ingredients
    private float getBasicScore( final IIngredient inIngr) {
        return inIngr.isOptional() ? 0.4f : 1.0f;
    }
}