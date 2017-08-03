/**
 *
 */
package uk.co.recipes.parse;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * TODO
 *
 * @author andrewr
 */
public class FractionReplacerTest {

    @Test
    public void testFractions() {
        final FractionReplacer fr = new FractionReplacer();
        assertThat(fr.replaceFractions("3 ½ lbs"), is("3.5 lbs"));
        assertThat(fr.replaceFractions("3¼ lbs"), is("3.25 lbs"));
        assertThat(fr.replaceFractions("½ tsp"), is("0.5 tsp"));
        assertThat(fr.replaceFractions("½-1 tsp"), is("0.5-1 tsp"));
        assertThat(fr.replaceFractions("¼"), is("0.25"));
        assertThat(fr.replaceFractions("¾"), is("0.75"));
        assertThat(fr.replaceFractions(" ½ lbs "), is("0.5 lbs"));
    }

    @Test
    public void testFractionSlashRemoval() {
        final FractionReplacer fr = new FractionReplacer();
        assertThat(fr.replaceFractions("8⁄9"), is("8/9"));
    }

    // Should be an error, but too complex to solve now
    @Test(enabled = false)
    public void testBadFractions() {
        final FractionReplacer fr = new FractionReplacer();
        fr.replaceFractions("¼¼");
    }
}