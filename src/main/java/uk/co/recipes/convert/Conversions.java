/**
 * 
 */
package uk.co.recipes.convert;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;

import javax.measure.quantity.Quantity;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * See http://www.traditionaloven.com/conversions_of_measures/flour_volume_weight.html
 * Also see http://allrecipes.com/howto/cup-to-gram-conversions/ - which differs significantly
 *
 * @author andrewregan
 *
 */
public class Conversions {

    private final static Locale DEFAULT_LOCALE = new Locale( "xx", "xx");
    private final static String DEFAULT_ITEM = "_";

    private final static Table<ItemUnit,Locale,Unit<?>>  VOLUME_UNITS_TABLE = HashBasedTable.create();

    static {
    	final Unit<Volume> tsp_UK = NonSI.LITRE.divide(1000).times(5.91939047);
    	final Unit<Volume> tsp_US = NonSI.LITRE.divide(1000).times(4.92892159);
    	final Unit<Volume> tbsp_UK = tsp_UK.times(3);
    	final Unit<Volume> tbsp_US = tsp_US.times(3);

        VOLUME_UNITS_TABLE.put( forUnit(Units.TSP), DEFAULT_LOCALE, tsp_UK);
        VOLUME_UNITS_TABLE.put( forUnit(Units.TBSP), DEFAULT_LOCALE, tbsp_UK);

        VOLUME_UNITS_TABLE.put( forUnit(Units.GRAMMES), DEFAULT_LOCALE, SI.GRAM);

        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Plain Flour"), /* Override */ Locale.US, SI.KILOGRAM.times(0.125));
        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Bread Flour"), /* Override */ Locale.US, SI.KILOGRAM.times(0.127));
        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Rye Flour"), /* Override */ Locale.US, SI.KILOGRAM.times(0.102));
        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Self-raising Flour"), /* Override */ Locale.US, SI.KILOGRAM.times(0.125));
        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Wholewheat Flour"), /* Override */ Locale.US, SI.KILOGRAM.times(0.120));
        VOLUME_UNITS_TABLE.put( new ItemUnit( Units.CUP, "Rolled Oats"), /* Override */ Locale.US, SI.KILOGRAM.times(0.085));

//        VOLUME_UNITS_TABLE.put( forUnit(Units.QUART), /* Override */ Locale.US, tsp_US);

        VOLUME_UNITS_TABLE.put( forUnit(Units.TSP), /* Override */ Locale.US, tsp_US);
        VOLUME_UNITS_TABLE.put( forUnit(Units.TBSP), /* Override */ Locale.US, tbsp_US);
    }

	public <T extends Quantity> Optional<Amount<T>> toJsrAmount( final Locale inLocale, final IQuantity inQuantity) {
		return toJsrAmount( inLocale, forUnit( inQuantity.getUnits() ), inQuantity);
	}

	public <T extends Quantity> Optional<Amount<T>> toJsrAmount( final Locale inLocale, final String inItemName, IQuantity inQuantity) {
		return toJsrAmount( inLocale, new ItemUnit( inQuantity.getUnits(), inItemName), inQuantity);
	}

	private <T extends Quantity> Optional<Amount<T>> toJsrAmount( final Locale inLocale, final ItemUnit inItemUnit, final IQuantity inQuantity) {

	    final Unit<?> unitForLocale = VOLUME_UNITS_TABLE.get( inItemUnit, inLocale);
	    if ( unitForLocale != null) {
            return amountInUnits( inQuantity, unitForLocale);
	    }

        final Unit<?> defaultUnit = VOLUME_UNITS_TABLE.get( forUnit( inQuantity.getUnits() ), DEFAULT_LOCALE);
        if ( defaultUnit != null) {
            return amountInUnits( inQuantity, defaultUnit);
        }

        // Yuk, FIXME!

        if ( inLocale == Locale.US) {
            if ( inItemUnit.unit == Units.QUART) {
            	final Optional<Amount<T>> opt = toJsrAmount( inLocale, new ItemUnit( Units.CUP, inItemUnit.canonicalName), inQuantity);
            	if (opt.isPresent()) {
            		return Optional.of(opt.get().times(4));  // http://en.wikipedia.org/wiki/Quart
            	}
            }
            else if ( inItemUnit.unit == Units.OUNCES) {
            	final Optional<Amount<T>> opt = toJsrAmount( inLocale, new ItemUnit( Units.CUP, inItemUnit.canonicalName), inQuantity);
            	if (opt.isPresent()) {
            		return Optional.of(opt.get().times(0.125));  // http://en.wikipedia.org/wiki/Fluid_ounce
            	}
            }
        }

		return Optional.absent();
	}

	@SuppressWarnings("unchecked")
	private <T extends Quantity> Optional<Amount<T>> amountInUnits( final IQuantity inQuantity, final Unit<?> inUnits) {
        return Optional.of((Amount<T>) Amount.valueOf( inQuantity.getNumber(), inUnits));
	}

	private static ItemUnit forUnit( final IUnit unit) {
		return new ItemUnit( unit, DEFAULT_ITEM);
	}

	/**
	 * 
	 * TODO
	 *
	 * @author andrewregan
	 *
	 */
	private static class ItemUnit {
		private IUnit unit;
		private String canonicalName;

		public ItemUnit( final IUnit unit, final String canonicalName) {
			this.unit = checkNotNull(unit);
			this.canonicalName = checkNotNull(canonicalName);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode( unit, canonicalName);
		}

		@Override
		public boolean equals( Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ItemUnit)) {
				return false;
			}
			final ItemUnit other = (ItemUnit) obj;
			return Objects.equal( unit, other.unit) && Objects.equal( canonicalName, other.canonicalName);
		}
	}
}
