/**
 * 
 */
package uk.co.recipes.convert;

import static java.util.Locale.UK;
import static java.util.Locale.US;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.testng.annotations.Test;

import uk.co.recipes.Quantity;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.Units;

import com.google.common.base.Optional;


/**
 * See http://www.traditionaloven.com/conversions_of_measures/flour_volume_weight.html
 * Also see http://allrecipes.com/howto/cup-to-gram-conversions/ - which differs significantly
 * 
 * @author andrewr
 *
 */
public class ConversionsTest {

    private final static Conversions CVS = new Conversions();

    private final static IQuantity ONE_CUP = new Quantity( Units.CUP, 1);
    private final static IQuantity ONE_QUART = new Quantity( Units.QUART, 1);
    private final static IQuantity ONE_OZ = new Quantity( Units.OUNCES, 1);
    private final static IQuantity ONE_TSP = new Quantity( Units.TSP, 1);
    private final static IQuantity ONE_HEAPED_TSP = new Quantity( Units.HEAPED_TSP, 1);
    private final static IQuantity ONE_ROUNDED_TSP = new Quantity( Units.ROUNDED_TSP, 1);
    private final static IQuantity ONE_TBSP = new Quantity( Units.TBSP, 1);
    private final static IQuantity ONE_HEAPED_TBSP = new Quantity( Units.HEAPED_TBSP, 1);
    private final static IQuantity ONE_ROUNDED_TBSP = new Quantity( Units.ROUNDED_TBSP, 1);
    private final static IQuantity NINETEEN_GRAMMES = new Quantity( Units.GRAMMES, 19);
    private final static IQuantity EIGHT_KG = new Quantity( Units.KG, 8);
    private final static IQuantity SIXTEEN_ML = new Quantity( Units.ML, 16);
    private final static IQuantity FIVE_LITRES = new Quantity( Units.LITRE, 5);

    @Test
    public void testGrammesUK() {
    	assertThat( toGrammes( CVS.toJsrAmount( UK, NINETEEN_GRAMMES)), is(19.0));
    }

    @Test
    public void testKilogrammesUK() {
    	assertThat( toGrammes( CVS.toJsrAmount( UK, EIGHT_KG)), nearTo(8000.0));
    }

    @Test
    public void testMlUK() {
    	assertThat( toMl( CVS.toJsrAmount( UK, SIXTEEN_ML)), is(16.0));
    }

    @Test
    public void testLitresUK() {
    	assertThat( toMl( CVS.toJsrAmount( UK, FIVE_LITRES)), nearTo(5000.0));
    }

    @Test
    public void testTspUK() {
    	assertThat( CVS.toJsrAmount( UK, ONE_TSP).get().getEstimatedValue(), is(1.0));
    }

    @Test
    public void testTspUKInMilliletres() {
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_TSP) ), nearTo(5.91939047));
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_HEAPED_TSP) ), nearTo(10.654903));
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_ROUNDED_TSP) ), nearTo(7.6952076));
    }

    @Test
    public void testTbspUKInMilliletres() {
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_TBSP) ), nearTo(17.758171));
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_HEAPED_TBSP) ), nearTo(31.964709));
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_ROUNDED_TBSP) ), nearTo(23.085623));
    }

    @Test
    public void testTspUS() {
        assertThat( CVS.toJsrAmount( US, ONE_TSP).get().getEstimatedValue(), is(1.0));
    }

    @Test
    public void testMissingUnit() {
        assertThat( CVS.toJsrAmount( UK, new Quantity( Units.SPLASHES, 1)).isPresent(), is(false));
    }

    @Test
    public void testFlour_USCups() {
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", ONE_CUP) ), nearTo(125d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", ONE_CUP) ), nearTo(127d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rye Flour", ONE_CUP) ), nearTo(102d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Self-raising Flour", ONE_CUP) ), nearTo(125d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Wholewheat Flour", ONE_CUP) ), nearTo(120d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rolled Oats", ONE_CUP) ), nearTo(85d));
    }

    @Test
    public void testFlour_USQuarts() {
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", ONE_QUART) ), nearTo(500d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", ONE_QUART) ), nearTo(508d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rye Flour", ONE_QUART) ), nearTo(408d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Self-raising Flour", ONE_QUART) ), nearTo(500d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Wholewheat Flour", ONE_QUART) ), nearTo(480d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rolled Oats", ONE_QUART) ), nearTo(340d));
    }

    @Test
    public void testFlour_OZ() {
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", /* Fluid */ ONE_OZ) ), nearTo(15.625));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", /* Fluid */ ONE_OZ) ), nearTo(15.875));
    }

    /**
     * Helpers
     */
    private double toGrammes( final Optional<Amount<javax.measure.quantity.Quantity>> amount) {
    	return amount.get().to( SI.KILOGRAM.divide(1000) ).getEstimatedValue();
    }

    private double toMl( final Optional<Amount<javax.measure.quantity.Quantity>> amount) {
    	return amount.get().to( NonSI.LITRE.divide(1000) ).getEstimatedValue();
    }

    // Deal with rounding issues
    private static org.hamcrest.Matcher<Double> nearTo( final double inVal) {
        return allOf( greaterThan( inVal - 1E-6), lessThan( inVal + 1E-6) );
    }
}
