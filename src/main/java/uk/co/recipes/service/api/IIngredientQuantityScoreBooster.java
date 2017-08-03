/**
 *
 */
package uk.co.recipes.service.api;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;

import java.util.Locale;

/**
 * @author andrewr
 */
public interface IIngredientQuantityScoreBooster {

    float getBoostForQuantity(final Locale inRecipeLocale, final ICanonicalItem inItem, final IQuantity inQuantity);
}
