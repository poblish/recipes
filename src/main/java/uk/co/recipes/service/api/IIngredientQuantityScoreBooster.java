/**
 * 
 */
package uk.co.recipes.service.api;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;

/**
 * @author andrewr
 *
 */
public interface IIngredientQuantityScoreBooster {

    float getBoostForQuantity( final ICanonicalItem inItem, final IQuantity inQuantity);
}
