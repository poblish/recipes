/**
 * 
 */
package uk.co.recipes.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Locale;

import javax.measure.unit.NonSI;

import org.testng.annotations.Test;

import uk.co.recipes.Quantity;
import uk.co.recipes.api.Units;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class ConversionsTest {

    private final static Conversions CVS = new Conversions();

    @Test
    public void testTspUK() {
        assertThat( CVS.toJsrAmount( Locale.UK, new Quantity( Units.TSP, 1)).get().getEstimatedValue(), is(1.0));
    }

    @Test
    public void testTspUKInMilliletres() {
        assertThat( CVS.toJsrAmount( Locale.UK, new Quantity( Units.TSP, 1)).get().to( NonSI.LITRE.divide(1000) ).getEstimatedValue(), is(5.91939047));
    }

    @Test
    public void testTspUS() {
        assertThat( CVS.toJsrAmount( Locale.US, new Quantity( Units.TSP, 1)).get().getEstimatedValue(), is(1.0));
    }

    @Test
    public void testMissingUnit() {
        assertThat( CVS.toJsrAmount( Locale.UK, new Quantity( Units.SPLASHES, 1)).isPresent(), is(false));
    }
}
