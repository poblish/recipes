package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.empty;
import dagger.ObjectGraph;
import uk.co.recipes.DaggerModule;
import org.testng.annotations.Test;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class NameAdjusterTest {

    private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

    @Test
    public void testAdjust1() {
        final NameAdjuster na = GRAPH.get( NameAdjuster.class );
        assertThat( na.adjust("dressed mixed leaves"), is("mixed leaves"));
        assertThat( na.getExtraNotes(), hasItems("dressed"));
    }

    @Test
    public void testAdjust2() {
        final NameAdjuster na = GRAPH.get( NameAdjuster.class );
        assertThat( na.adjust("freshly grated nutmeg"), is("nutmeg"));
        assertThat( na.getExtraNotes(), hasItems("freshly grated"));
    }

    @Test
    public void testAdjust3() {
        final NameAdjuster na = GRAPH.get( NameAdjuster.class );
        assertThat( na.adjust("whole mango"), is("mango"));
        assertThat( na.getExtraNotes(), hasItems("whole"));
    }

    @Test
    public void testAdjust4() {
        final NameAdjuster na = GRAPH.get( NameAdjuster.class );
        assertThat( na.adjust("FuLL-bODIEd RED WINE"), is("RED WINE"));
        assertThat( na.getExtraNotes(), hasItems("full-bodied"));
    }

    @Test
    public void testAdjust5() {
        final NameAdjuster na = GRAPH.get( NameAdjuster.class );
        assertThat( na.adjust("100g coriander seeds"), is("100g coriander seeds"));
        assertThat( na.getExtraNotes(), empty());
    }
}
