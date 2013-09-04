/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.Locale;

import javax.inject.Singleton;
import javax.measure.converter.RationalConverter;
import javax.measure.quantity.Volume;
import javax.measure.unit.Unit;

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

    @Override
    public float getBoostForQuantity( final ICanonicalItem inItem, final IQuantity inQuantity) {
    	if ( inItem.getBaseAmount() != null /* FIXME null check */) {
			System.out.println( "=> " + inQuantity );

			final Optional<Unit<Volume>> units = new Conversions().getUnitOfVolume( Locale.UK, inQuantity);
    		if (units.isPresent()) {

    			Optional<Unit<Volume>> baseUnits = new Conversions().getUnitOfVolume( Locale.UK, inItem.getBaseAmount());
        		if (baseUnits.isPresent()) {
        			System.out.println( units.get() + " / " + baseUnits.get() );
        			Unit<?> ratio = units.get().divide( baseUnits.get() );
        			System.out.println( "Boost = " + ratio.getDimension() );
        			int x = 1;
        		}
    		}
    		// System.out.println(inItem);
    	}
        return NO_BOOST;
    }
}