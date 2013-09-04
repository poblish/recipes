/**
 * 
 */
package uk.co.recipes.convert;


import java.util.Locale;

import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.Units;

import com.google.common.base.Optional;

/**
 * See http://www.traditionaloven.com/conversions_of_measures/flour_volume_weight.html any many others.
 *
 * @author andrewregan
 *
 */
public class Conversions {

	private final static Unit<Volume> TSP_UK = NonSI.LITRE.divide(1000).times(5.91939047);
	private final static Unit<Volume> TSP_US = NonSI.LITRE.divide(1000).times(4.92892159);

	public Optional<Unit<Volume>> getUnitOfVolume( final Locale inLocale, final IQuantity inQuantity) {
		if ( inQuantity.getUnits() == Units.TSP) {
			if ( inLocale == Locale.UK) {
				return Optional.of( times( TSP_UK, inQuantity.getNumber() ) );
			}
			else {  // US is default, is it now?
				return Optional.of( times( TSP_US, inQuantity.getNumber() ) );
			}
		}

		return Optional.absent();
	}

	// Work around crap code
	private Unit<Volume> times( Unit<Volume> unit, int inCount) {
		if ( inCount == 1) {
			return unit;
		}

		return unit.times(inCount);
	}
}
