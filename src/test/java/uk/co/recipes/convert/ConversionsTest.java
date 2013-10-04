/**
 * 
 */
package uk.co.recipes.convert;

import static java.util.Locale.UK;
import static java.util.Locale.US;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

    @Test
    public void testTspUK() {
    	assertThat( CVS.toJsrAmount( UK, ONE_TSP).get().getEstimatedValue(), is(1.0));
    }

    @Test
    public void testTspUKInMilliletres() {
        assertThat( toMl( CVS.toJsrAmount( UK, ONE_TSP) ), is(5.91939047));
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
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", ONE_CUP) ), is(125d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", ONE_CUP) ), is(127d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rye Flour", ONE_CUP) ), is(102d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Self-raising Flour", ONE_CUP) ), is(125d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Wholewheat Flour", ONE_CUP) ), is(120d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rolled Oats", ONE_CUP) ), is(85d));
    }

    @Test
    public void testFlour_USQuarts() {
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", ONE_QUART) ), is(500d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", ONE_QUART) ), is(508d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rye Flour", ONE_QUART) ), is(408d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Self-raising Flour", ONE_QUART) ), is(500d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Wholewheat Flour", ONE_QUART) ), is(480d));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Rolled Oats", ONE_QUART) ), is(340d));
    }

    @Test
    public void testFlour_OZ() {
        assertThat( toGrammes( CVS.toJsrAmount( US, "Plain Flour", /* Fluid */ ONE_OZ) ), is(15.624999999999998));
        assertThat( toGrammes( CVS.toJsrAmount( US, "Bread Flour", /* Fluid */ ONE_OZ) ), is(15.874999999999998));
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
}
