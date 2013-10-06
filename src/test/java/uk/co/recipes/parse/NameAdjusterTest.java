package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class NameAdjusterTest {

    private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
    private final static NameAdjuster ADJUSTER = GRAPH.get( NameAdjuster.class );

    @Test
    public void testAdjust1() {
        final AdjustedName na = ADJUSTER.adjust("dressed mixed leaves");
        assertThat( na.getName(), is("mixed leaves"));
        assertThat( na.getNotes(), hasItems("dressed"));
    }

    @Test
    public void testAdjust2() {
        final AdjustedName na = ADJUSTER.adjust("freshly grated nutmeg");
        assertThat( na.getName(), is("nutmeg"));
        assertThat( na.getNotes(), hasItems("freshly grated"));
    }

    @Test
    public void testAdjust3() {
        final AdjustedName na = ADJUSTER.adjust("whole mango");
        assertThat( na.getName(), is("mango"));
        assertThat( na.getNotes(), hasItems("whole"));
    }

    @Test
    public void testAdjust4() {
        final AdjustedName na = ADJUSTER.adjust("FuLL-bODIEd RED WINE");
        assertThat( na.getName(), is("RED WINE"));
        assertThat( na.getNotes(), hasItems("full-bodied"));
    }

    @Test
    public void testAdjust5() {
        final AdjustedName na = ADJUSTER.adjust("100g coriander seeds");
        assertThat( na.getName(), is("100g coriander seeds"));
        assertThat( na.getNotes(), empty());
    }

    @Test
    public void testAdjustSuffix1() {
        final AdjustedName na = ADJUSTER.adjust("olive oil for drizzling");
        assertThat( na.getName(), is("olive oil"));
        assertThat( na.getNotes(), hasItems("for drizzling"));
    }

    @Test
    public void testAdjustPrefixAndSuffixes() {
        final AdjustedName na = ADJUSTER.adjust("low-sodium walnut pieces for drizzling");
        assertThat( na.getName(), is("walnut"));
        assertThat( na.getNotes(), hasItems("for drizzling", "pieces"));
    }
}
