/**
 * 
 */
package uk.co.recipes.events.impl;

import java.util.Set;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;

import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class DefaultEventService implements IEventService {

	private Set<IEventListener> listeners = Sets.newHashSet();

    public void addListener( final IEventListener inL) {
        listeners.add(inL);
    }

    public void addItem( final ICanonicalItem inItem) {
        for ( IEventListener each : listeners) {
            each.onAddItem( new ItemEvent( null, inItem, 1.0f) );
        }
    }

    public void deleteItem( final ICanonicalItem inItem) {
        for ( IEventListener each : listeners) {
            each.onDeleteItem( new ItemEvent( null, inItem, 1.0f) );
        }
    }

    public void addRecipe( final IRecipe inRecipe) {
        for ( IEventListener each : listeners) {
            each.onAddRecipe( new RecipeEvent( null, inRecipe, 1.0f) );
        }
    }

    public void deleteRecipe( final IRecipe inRecipe) {
        for ( IEventListener each : listeners) {
            each.onDeleteRecipe( new RecipeEvent( null, inRecipe, 1.0f) );
        }
    }

    public void rateItem( final IUser inUser, final ICanonicalItem inItem) {
    	rateItem( inUser, inItem, 1.0f);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.events.api.IEventService#rateItem(uk.co.recipes.api.IUser, uk.co.recipes.api.ICanonicalItem, float)
	 */
	@Override
	public void rateItem( IUser inUser, ICanonicalItem inItem, float inRating) {
		for ( IEventListener each : listeners) {
			each.onRateItem( new ItemEvent( inUser, inItem, inRating) );
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.events.api.IEventService#rateRecipe(uk.co.recipes.api.IUser, uk.co.recipes.api.IRecipe)
	 */
	@Override
	public void rateRecipe( final IUser inUser, final IRecipe inRecipe) {
		rateRecipe( inUser, inRecipe, 1.0f);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.events.api.IEventService#rateRecipe(uk.co.recipes.api.IUser, uk.co.recipes.api.IRecipe, float)
	 */
	@Override
	public void rateRecipe( IUser inUser, IRecipe inRecipe, float inRating) {
		for ( IEventListener each : listeners) {
			each.onRateRecipe( new RecipeEvent( inUser, inRecipe, inRating) );
		}
	}
}