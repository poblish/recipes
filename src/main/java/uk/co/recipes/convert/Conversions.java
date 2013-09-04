/**
 * 
 */
package uk.co.recipes.convert;


import java.util.Locale;

import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * See http://www.traditionaloven.com/conversions_of_measures/flour_volume_weight.html any many others.
 *
 * @author andrewregan
 *
 */
public class Conversions {

    // Can probably fold these into the VOLUME_UNITS_TABLE init.
	private final static Unit<Volume> TSP_UK = NonSI.LITRE.divide(1000).times(5.91939047);
	private final static Unit<Volume> TSP_US = NonSI.LITRE.divide(1000).times(4.92892159);
    private final static Unit<Volume> TBSP_UK = TSP_UK.times(3);
    private final static Unit<Volume> TBSP_US = TSP_US.times(3);

    private final static Locale DEFAULT_LOCALE = new Locale( "xx", "xx");

    private final static Table<IUnit,Locale,Unit<Volume>>  VOLUME_UNITS_TABLE = HashBasedTable.create();

    static {
        VOLUME_UNITS_TABLE.put( Units.TSP, DEFAULT_LOCALE, TSP_UK);
        VOLUME_UNITS_TABLE.put( Units.TBSP, DEFAULT_LOCALE, TBSP_UK);

        VOLUME_UNITS_TABLE.put( Units.TSP, /* Override */ Locale.US, TSP_US);
        VOLUME_UNITS_TABLE.put( Units.TBSP, /* Override */ Locale.US, TBSP_US);
    }

	public Optional<Amount<Volume>> getUnitOfVolume( final Locale inLocale, final IQuantity inQuantity) {

	    final Unit<Volume> unitForLocale = VOLUME_UNITS_TABLE.get( inQuantity.getUnits(), inLocale);
	    if ( unitForLocale != null) {
            return amountInUnits( inQuantity, unitForLocale);
	    }

        final Unit<Volume> defaultUnit = VOLUME_UNITS_TABLE.get( inQuantity.getUnits(), DEFAULT_LOCALE);
        if ( defaultUnit != null) {
            return amountInUnits( inQuantity, defaultUnit);
        }

		return Optional.absent();
	}

	private Optional<Amount<Volume>> amountInUnits( final IQuantity inQuantity, final Unit<Volume> inUnits) {
        return Optional.of( Amount.valueOf( inQuantity.getNumber(), inUnits) );
	}
}
