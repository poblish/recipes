package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class NameAdjusterTest {
	@Test
	public void testAdjust() {
		assertThat( new NameAdjuster().adjust("dressed mixed leaves"), is("mixed leaves"));
	}
}
