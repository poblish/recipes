/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.Locale;

import javax.inject.Singleton;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.convert.Conversions;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;

import com.google.common.base.Optional;


/**
 * @author andrewr
 *
 */
@Singleton
public class DefaultIngredientQuantityScoreBooster implements IIngredientQuantityScoreBooster {

    private static final float NO_BOOST = 1.0f;
//    private static final float FILTER_OUT = 0;

	private static final Logger LOG = LoggerFactory.getLogger( DefaultIngredientQuantityScoreBooster.class );

    @Override
    public float getBoostForQuantity( final Locale inRecipeLocale, final ICanonicalItem inItem, final IQuantity inQuantity) {

        if ( inQuantity.getNumber() <= 0) {
            return NO_BOOST;  // Safe, but wrong (see below)
            // FIXME Should do this, once parsing is much improved... return FILTER_OUT;  // Zero quantity passed-in, no point continuing with that. Quantities shouldn't be zero.
        }

    	if (inItem.getBaseAmount().isPresent()) {
			final Optional<Amount<Volume>> actualAmount = new Conversions().toJsrAmount( inRecipeLocale, inQuantity);
    		if (actualAmount.isPresent()) {
    			final Optional<Amount<Volume>> baseAmount = new Conversions().toJsrAmount( inRecipeLocale, inItem.getBaseAmount().get());
        		if (baseAmount.isPresent()) {
        			final Amount<?> ratio = actualAmount.get().divide( baseAmount.get() );
                    // System.out.println( "Boost = " + ratio.getEstimatedValue() + " for " + inItem);
                    return (float) ratio.getEstimatedValue();
        		}
    		}
    		else {
    			LOG.warn("No conversion found for " + inQuantity + " of " + inItem);
    		}
    	}

    	return NO_BOOST;
    }
}   