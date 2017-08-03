package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

public class NumericAmountParserTest {

	@Test 
	public void testParsing() {
		assertThat( NumericAmountParser.parse("3 x 400"), is(1200.0));
		assertThat( NumericAmountParser.parse("4x 400"), is(1600.0));
		assertThat( NumericAmountParser.parse("2-3 x 375"), is(937.5));
		assertThat( NumericAmountParser.parse("2-3"), is(2.5));
		assertThat( NumericAmountParser.parse("1/4"), is(0.25));
		assertThat( NumericAmountParser.parse("1.37"), is(1.37));
	}
}
