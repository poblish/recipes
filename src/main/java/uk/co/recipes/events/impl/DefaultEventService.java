package uk.co.recipes.events.impl;

import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;

public class DefaultEventService implements IEventService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventService.class);

    private final EventBus eventBus;

    public DefaultEventService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void addListener(final IEventListener inL) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("=> Adding Listener " + inL + " to " + eventBus);
        }
        eventBus.register(inL);
    }

    public void addItem(final ICanonicalItem inItem) {
        eventBus.post(new AddItemEvent(inItem));
    }

    public void deleteItem(final ICanonicalItem inItem) {
        eventBus.post(new DeleteItemEvent(inItem));
    }

    public void addRecipe(final IRecipe inRecipe) {
        eventBus.post(new AddRecipeEvent(inRecipe));
    }

    public void deleteRecipe(final IRecipe inRecipe) {
        eventBus.post(new DeleteRecipeEvent(inRecipe));
    }

    public void rateItem(final IUser inUser, final ICanonicalItem inItem) {
        rateItem(inUser, inItem, 1.0f);
    }

    @Override
    public void rateItem(IUser inUser, ICanonicalItem inItem, float inRating) {
        eventBus.post(new RateItemEvent(inUser, inItem, inRating));
    }

    @Override
    public void faveItem(IUser inUser, ICanonicalItem inItem) {
        eventBus.post(new FaveItemEvent(inUser, inItem));
    }

    @Override
    public void unFaveItem(IUser inUser, ICanonicalItem inItem) {
        eventBus.post(new UnFaveItemEvent(inUser, inItem));
    }

    @Override
    public void faveRecipe(IUser inUser, IRecipe inRecipe) {
        eventBus.post(new FaveRecipeEvent(inUser, inRecipe));
    }

    @Override
    public void unFaveRecipe(IUser inUser, IRecipe inRecipe) {
        eventBus.post(new UnFaveRecipeEvent(inUser, inRecipe));
    }

    @Override
    public void rateRecipe(final IUser inUser, final IRecipe inRecipe) {
        rateRecipe(inUser, inRecipe, 1.0f);
    }

    @Override
    public void rateRecipe(IUser inUser, IRecipe inRecipe, float inRating) {
        eventBus.post(new RateRecipeEvent(inUser, inRecipe, inRating));
    }

    @Override
    public void addRecipeIngredients(IRecipe inRecipe, IIngredient... inIngredients) {
        for (IIngredient item : inIngredients) {
            eventBus.post(new RecipeAddIngredientsEvent(inRecipe, item));
        }
    }

    @Override
    public void removeRecipeIngredients(IRecipe inRecipe, final IIngredient... inIngredients) {
        for (IIngredient item : inIngredients) {
            eventBus.post(new RecipeRemoveIngredientsEvent(inRecipe, item));
        }
    }

    @Override
    public void visit(IUser inUser, ICanonicalItem inItem) {
        eventBus.post(new ItemVisitedEvent(inUser, inItem));
    }

    @Override
    public void visit(IUser inUser, IRecipe inRecipe) {
        eventBus.post(new RecipeVisitedEvent(inUser, inRecipe));
    }
}