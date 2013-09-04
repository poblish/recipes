/**
 * 
 */
package uk.co.recipes.convert;


import org.jscience.physics.amount.Amount;
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

	public Optional<Amount<Volume>> getUnitOfVolume( final Locale inLocale, final IQuantity inQuantity) {
		if ( inQuantity.getUnits() == Units.TSP) {
			if ( inLocale == Locale.UK) {
				return amountInUnits( inQuantity, TSP_UK);
			}
			else {  // US is default, is it now?
				return amountInUnits( inQuantity, TSP_US);
			}
		}

		return Optional.absent();
	}

	private Optional<Amount<Volume>> amountInUnits( final IQuantity inQuantity, final Unit<Volume> inUnits) {
        return Optional.of( Amount.valueOf( inQuantity.getNumber(), TSP_US) );
	}
}
