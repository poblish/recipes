/**
 * 
 */
package uk.co.recipes.service.impl;

import javax.inject.Singleton;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;


/**
 * @author andrewr
 *
 */
@Singleton
public class DefaultIngredientQuantityScoreBooster implements IIngredientQuantityScoreBooster {

    private static final float NO_BOOST = 1.0f;

    @Override
    public float getBoostForQuantity( final ICanonicalItem inItem, final IQuantity inQuantity) {
        return NO_BOOST;
    }
}