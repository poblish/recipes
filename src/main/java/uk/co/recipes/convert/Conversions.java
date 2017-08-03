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
   
    private final static double ROUNDED_FACTOR = 1.3;	// Guessing, after http://www.hintsandthings.co.uk/kitchen/measures.htm
    private final static double HEAPED_FACTOR = 1.8;	// " " "

    private final static Table<ItemUnit,Locale,Unit<?>>  VOLUME_UNITS_TABLE = HashBasedTable.create();

    static {
    	final Unit<Volume> millilitre = NonSI.LITRE.divide(1000);
    	final Unit<Volume> tsp_UK = millilitre.times(5.91939047);
    	final Unit<Volume> tsp_US = millilitre.times(4.92892159);

    	defaults( Units.TSP, tsp_UK);
    	defaults( Units.ROUNDED_TSP, tsp_UK.times(ROUNDED_FACTOR));
    	defaults( Units.HEAPED_TSP, tsp_UK.times(HEAPED_FACTOR));
    	defaults( Units.TBSP, tsp_UK.times(3));
    	defaults( Units.ROUNDED_TBSP, tsp_UK.times(3).times(ROUNDED_FACTOR));
    	defaults( Units.HEAPED_TBSP, tsp_UK.times(3).times(HEAPED_FACTOR));

    	defaults( Units.GRAMMES, SI.GRAM);
    	defaults( Units.KG, SI.KILOGRAM);
    	defaults( Units.LITRE, NonSI.LITRE);
    	defaults( Units.ML, millilitre);

        override( Locale.US, new ItemUnit( Units.CUP, "Plain Flour"), SI.KILOGRAM.times(0.125));
        override( Locale.US, new ItemUnit( Units.CUP, "Bread Flour"), SI.KILOGRAM.times(0.127));
        override( Locale.US, new ItemUnit( Units.CUP, "Rye Flour"), SI.KILOGRAM.times(0.102));
        override( Locale.US, new ItemUnit( Units.CUP, "Self-raising Flour"), SI.KILOGRAM.times(0.125));
        override( Locale.US, new ItemUnit( Units.CUP, "Wholewheat Flour"), SI.KILOGRAM.times(0.120));
        override( Locale.US, new ItemUnit( Units.CUP, "Rolled Oats"), SI.KILOGRAM.times(0.085));

        override( Locale.US, Units.TSP, tsp_US);
        override( Locale.US, Units.ROUNDED_TSP, tsp_US.times(ROUNDED_FACTOR));
        override( Locale.US, Units.HEAPED_TSP, tsp_US.times(HEAPED_FACTOR));
        override( Locale.US, Units.TBSP, tsp_US.times(3));
        override( Locale.US, Units.ROUNDED_TBSP, tsp_US.times(3).times(ROUNDED_FACTOR));
        override( Locale.US, Units.HEAPED_TBSP, tsp_US.times(3).times(HEAPED_FACTOR));

//      override( Locale.US, Units.QUART, tsp_US);
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

    @SuppressWarnings("unused")
    private static void defaults( ItemUnit itemUnit, Unit<?> inUnit) {
        VOLUME_UNITS_TABLE.put( itemUnit, DEFAULT_LOCALE, inUnit);
    }

    private static void defaults( IUnit unit, Unit<?> inUnit) {
        VOLUME_UNITS_TABLE.put( forUnit(unit), DEFAULT_LOCALE, inUnit);
    }

    private static void override( Locale inLocale, ItemUnit itemUnit, Unit<?> inUnit) {
        VOLUME_UNITS_TABLE.put( itemUnit, inLocale, inUnit);
    }

    private static void override( Locale inLocale, IUnit unit, Unit<?> inUnit) {
        VOLUME_UNITS_TABLE.put( forUnit(unit), inLocale, inUnit);
    }

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
