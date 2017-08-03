/**
 *
 */
package uk.co.recipes.events.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Floats;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.tags.RecipeTags;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @author andrewregan
 */
@Singleton
public class MyrrixUpdater implements IEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(MyrrixUpdater.class);

    @Inject
    EsItemFactory itemFactory;  // Purely for getting Item Ids

    @Inject
    IEventService eventService;
    @Inject
    ClientRecommender recommender;
    @Inject
    IIngredientQuantityScoreBooster booster;

    private final static float DEFAULT_WEIGHT = 1.0f;
    private final static float OPTIONAL_INGREDIENT_WEIGHT = 0.4f;
    private final static float MATCHING_RECIPE_CATEGORY_WEIGHT = 0.6f;
    private final static float INGREDIENT_CONSTITUENT_WEIGHT = 0.25f;  // Pretty low, but bear in mind that if a constit is tagged with 'MEAT', that could still pull in +10

    @Inject
    public MyrrixUpdater() {
        // For Dagger
    }

    // Yuk: why can't Dagger do @PostConstruct ?
    public void startListening() {
        checkNotNull(eventService).addListener(this);
    }

    @Subscribe
    public void onAddItem(final AddItemEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onAddItem: " + evt);
        }

        boolean changesMade = false;

        try {
            changesMade |= setItemTagsForItem(evt.getItem(), evt.getItem().getId(), DEFAULT_WEIGHT);
            // NB. Do *not* deal with constituents here, as all items will be covered Item-level tagging, we don't want double-counting
        } catch (TasteException e) {
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
    public void onDeleteItem(final DeleteItemEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteItem: " + evt);
        }

        boolean changesMade = false;

        try {
            changesMade |= setItemTagsForItem(evt.getItem(), evt.getItem().getId(), -DEFAULT_WEIGHT);
            // NB. Do *not* deal with constituents here, as all items will be covered Item-level tagging, we don't want double-counting
        } catch (TasteException e) {
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
    public void onAddRecipe(final AddRecipeEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onAddRecipe: " + evt);
        }

        addRecipeIngredients(evt.getRecipe().getId(), evt.getRecipe().getLocale(), evt.getRecipe().getTags(), evt.getRecipe().getIngredients());
    }

    @Subscribe
    public void onDeleteRecipe(final DeleteRecipeEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteRecipe: " + evt);
        }

        removeRecipeIngredients(evt.getRecipe().getId(), evt.getRecipe().getLocale(), evt.getRecipe().getTags(), evt.getRecipe().getIngredients());
    }

    @Subscribe
    public void onAddRecipeIngredients(final RecipeAddIngredientsEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onAddRecipeIngredients: " + evt);
        }

        addRecipeIngredients(evt.getRecipe().getId(), evt.getRecipe().getLocale(), evt.getRecipe().getTags(), Lists.newArrayList(evt.getIngredient()));
    }

    @Subscribe
    public void onRemoveRecipeIngredients(final RecipeRemoveIngredientsEvent evt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("onDeleteRecipeIngredients: " + evt);
        }

        removeRecipeIngredients(evt.getRecipe().getId(), evt.getRecipe().getLocale(), evt.getRecipe().getTags(), Lists.newArrayList(evt.getIngredient()));
    }

    private void addRecipeIngredients(final long inRecipeId, final Locale inRecipeLocale, final Map<ITag,Serializable> inRecipeTags, final Collection<IIngredient> inIngredients) {
        boolean changesMade = false;
        final StringBuffer myrrixPrefsBuf = new StringBuffer();

        try {
            for (IIngredient eachIngr : inIngredients) {
                final float basicScoreForIngr = getBasicScore(eachIngr) * booster.getBoostForQuantity(inRecipeLocale, eachIngr.getItem(), eachIngr.getQuantity());

                changesMade |= setItemTagsForItem(eachIngr.getItem(), inRecipeId, basicScoreForIngr);

                // We've covered the ingredient itself, now give a little bit of credit to its constituent items
                for (ICanonicalItem eachConstituent : eachIngr.getItem().getConstituents()) {
                    changesMade |= setItemTagsForItem(eachConstituent, inRecipeId, basicScoreForIngr * INGREDIENT_CONSTITUENT_WEIGHT);
                }

                ///////////////////  Deal with preferences for Recipe > Item recommendations

                addPrefsForItem(myrrixPrefsBuf, inRecipeId, eachIngr.getItem(), basicScoreForIngr);
                changesMade = true;  // There will always be changes now - cheerfully overwrite flag
            }

            // Recipe Tags begin...
            final Serializable catVal = inRecipeTags.get(RecipeTags.RECIPE_CATEGORY);
            if (catVal != null) {
                changesMade |= doSetTag("RECIPE_CATEGORY_" + catVal.toString(), "SET", inRecipeId, /* No boost, I think */ MATCHING_RECIPE_CATEGORY_WEIGHT);
            }
            // Recipe Tags END

            if (changesMade) {
                recommender.ingest(new StringReader(myrrixPrefsBuf.toString()));
                recommender.refresh();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("addRecipeIngredients: refresh done");
                }
            }
        } catch (TasteException e) {
            Throwables.propagate(e);
        }
    }

    private void removeRecipeIngredients(final long inRecipeId, final Locale inRecipeLocale, final Map<ITag,Serializable> inRecipeTags, final Collection<IIngredient> inIngredients) {
        boolean changesMade = false;
        final StringBuffer myrrixPrefsBuf = new StringBuffer();

        try {
            for (IIngredient eachIngr : inIngredients) {
                final float basicScoreForIngr = -getBasicScore(eachIngr) * booster.getBoostForQuantity(inRecipeLocale, eachIngr.getItem(), eachIngr.getQuantity());

                changesMade |= setItemTagsForItem(eachIngr.getItem(), inRecipeId, basicScoreForIngr);

                // We've covered the ingredient itself, now remove a little bit of credit for its constituent items
                for (ICanonicalItem eachConstituent : eachIngr.getItem().getConstituents()) {
                    changesMade |= setItemTagsForItem(eachConstituent, inRecipeId, basicScoreForIngr * INGREDIENT_CONSTITUENT_WEIGHT);
                }

                ///////////////////  Deal with preferences for Recipe > Item recommendations

                addPrefsForItem(myrrixPrefsBuf, inRecipeId, eachIngr.getItem(), basicScoreForIngr);
                changesMade = true;  // There will always be changes now - cheerfully overwrite flag
            }

            // Recipe Tags begin...
            final Serializable catVal = inRecipeTags.get(RecipeTags.RECIPE_CATEGORY);
            if (catVal != null) {
                changesMade |= doSetTag("RECIPE_CATEGORY_" + catVal.toString(), "SET", inRecipeId, /* No boost, I think */ -MATCHING_RECIPE_CATEGORY_WEIGHT);
            }
            // Recipe Tags END

            if (changesMade) {
                recommender.ingest(new StringReader(myrrixPrefsBuf.toString()));
                recommender.refresh();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("deleteRecipeIngredients: refresh done");
                }
            }
        } catch (TasteException e) {
            Throwables.propagate(e);
        }
    }

    private void addPrefsForItem(final StringBuffer ioBuf, final long inRecipeId, final ICanonicalItem inIngrItem, final float inBaseScore) {
        if (ioBuf.length() > 0) {
            ioBuf.append("\n");
        }

        ioBuf.append(inRecipeId).append(",").append(inIngrItem.getId()).append(",").append(inBaseScore);

        for (ICanonicalItem eachConstituent : inIngrItem.getConstituents()) {
            ioBuf.append("\n").append(inRecipeId).append(",").append(eachConstituent.getId()).append(",").append(inBaseScore * INGREDIENT_CONSTITUENT_WEIGHT);
        }
    }

    private boolean setItemTagsForItem(final ICanonicalItem inItem, final long inItemOrRecipeId, final float inBasicScore) throws TasteException {
        boolean changesMade = setHierarchicalSimilarityTags(inItem, inItemOrRecipeId, inBasicScore);

        final String setStr = (inBasicScore > 0 ? "SET" : "UNSET");

        for (final Entry<ITag,Serializable> eachTag : inItem.getTags().entrySet()) {
            if (eachTag.getValue() instanceof Boolean) {
                if ((Boolean) eachTag.getValue()) {
                    // Don't bother setting if == FALSE

                    final float scoreToUse = eachTag.getKey().getBoost() * inBasicScore;
                    if (isPointlessScore(scoreToUse)) {
                        continue;
                    }

                    changesMade |= doSetTag(eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
                }
            } else {
                final Float fVal = Floats.tryParse((String) eachTag.getValue());
                if (fVal != null) {  // Got a float - treat it as a score
                    final float scoreToUse = /* Think we need the boost...? */ eachTag.getKey().getBoost() * fVal;
                    if (isPointlessScore(scoreToUse)) {
                        continue;
                    }

                    changesMade |= doSetTag(eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
                } else {  // It's a String value, e.g. scoville - ignore the value and just use basicScore
                    final float scoreToUse = eachTag.getKey().getBoost() * inBasicScore;
                    if (isPointlessScore(scoreToUse)) {
                        continue;
                    }

                    changesMade |= doSetTag(eachTag.getKey(), setStr, inItemOrRecipeId, scoreToUse);
                }
            }
        }

        return changesMade;
    }

    private boolean doSetTag(final ITag inTag, final String inSetStr, final long inItemOrRecipeId, final float inScoreToUse) throws TasteException {
        return doSetTag(inTag.toString(), inSetStr, inItemOrRecipeId, inScoreToUse);
    }

    private boolean doSetTag(final String inString, final String inSetStr, final long inItemOrRecipeId, final float inScoreToUse) throws TasteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(inSetStr + " Tag '" + inString + "' val=" + inScoreToUse + " for " + inItemOrRecipeId);
        }
        recommender.setItemTag(inString, inItemOrRecipeId, inScoreToUse);
        return true;
    }

    private boolean setHierarchicalSimilarityTags(final ICanonicalItem inItem, final long inItemOrRecipeId, final float inScore) {

        if (isPointlessScore(inScore)) {
            return false;
        }

        boolean changesMade = false;

        final String ourPseudoParentTagName = "PARENT_" + itemFactory.toStringId(inItem);

        if (LOG.isDebugEnabled()) {
            LOG.debug((inScore > 0 ? "SET" : "UNSET") + " Tag '" + ourPseudoParentTagName + "' val=" + inScore + " for " + inItemOrRecipeId);
        }
        // recommender.setItemTag( ourPseudoParentTagName, inItemOrRecipeId, inScore);

        if (inItem.parent().isPresent()) {
            changesMade |= setHierarchicalSimilarityTags(inItem.parent().get(), inItemOrRecipeId, inScore * 0.8f);
        }

        return changesMade;
    }

    private boolean isPointlessScore(final float inScore) {
        return inScore > -0.01f && inScore < 0.01f;
    }

    @Subscribe
    public void onVisitItem(final ItemVisitedEvent evt) {
        /* Comment-out for now until we have a better solution. Maybe we should defer these (e.g. log in Session)
    	 * to prevent ratings changing on refresh?

		if (LOG.isDebugEnabled()) {
			LOG.debug("Visited: " + evt);
		}

        rateGenericItem( evt.getUser().getId(), evt.getItem().getId(), evt.getScore());
       */
    }

    @Subscribe
    public void onVisitRecipe(final RecipeVisitedEvent evt) {
    	/* Comment-out for now until we have a better solution. Maybe we should defer these (e.g. log in Session)
    	 * to prevent ratings changing on refresh?

		if (LOG.isDebugEnabled()) {
			LOG.debug("Visited: " + evt);
		}

        rateGenericItem( evt.getUser().getId(), evt.getRecipe().getId(), evt.getScore());
       */
    }

    @Subscribe
    public void onRateItem(final RateItemEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rate: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getItem().getId(), evt.getScore());
    }

    @Subscribe
    public void onRateRecipe(final RateRecipeEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rate: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getRecipe().getId(), evt.getScore());
    }

    @Subscribe
    public void onFaveItem(final FaveItemEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fave: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getItem().getId(), evt.getScore());
    }

    @Subscribe
    public void onUnFaveItem(final UnFaveItemEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("UnFave: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getItem().getId(), evt.getScore());
    }

    @Subscribe
    public void onFaveRecipe(final FaveRecipeEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fave: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getRecipe().getId(), evt.getScore());
    }

    @Subscribe
    public void onUnFaveRecipe(final UnFaveRecipeEvent evt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("UnFave: " + evt);
        }

        rateGenericItem(evt.getUser().getId(), evt.getRecipe().getId(), evt.getScore());
    }

    private void rateGenericItem(final long inUserId, final long inGenericItemId, final float inRating) {
        try {
            recommender.ingest(new StringReader(inUserId + "," + inGenericItemId + "," + inRating));
            recommender.refresh();
        } catch (TasteException e) {
            Throwables.propagate(e);
        }
    }

    // For Recipe-based similarity (tags) and recommendations (prefs): de-value optional ingredients
    private float getBasicScore(final IIngredient inIngr) {
        return inIngr.isOptional() ? OPTIONAL_INGREDIENT_WEIGHT : DEFAULT_WEIGHT;
    }
}