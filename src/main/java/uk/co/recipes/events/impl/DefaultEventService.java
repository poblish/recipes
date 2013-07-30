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

    public void addItem( final ICanonicalItem inItem) {
        for ( IEventListener each : listeners) {
            each.onAddItem( new ItemEvent( null, inItem) );
        }
    }

    public void rateItem( final IUser inUser, final ICanonicalItem inItem) {
		for ( IEventListener each : listeners) {
			each.onRateItem( new ItemEvent( inUser, inItem) );
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.events.api.IEventService#rateRecipe(uk.co.recipes.api.IUser, uk.co.recipes.api.IRecipe)
	 */
	@Override
	public void rateRecipe( final IUser inUser, final IRecipe inRecipe) {
		for ( IEventListener each : listeners) {
			each.onRateRecipe( new RecipeEvent( inUser, inRecipe) );
		}
	}

    public void addListener( final IEventListener inL) {
        listeners.add(inL);
    }
}