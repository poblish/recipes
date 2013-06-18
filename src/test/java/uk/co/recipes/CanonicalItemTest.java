/**
 * 
 */
package uk.co.recipes;

import org.testng.annotations.Test;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItemTest {

	@Test
	public void testEqualsHash() {
		final CanonicalItem orig = new CanonicalItem("Lamb");
		final CanonicalItem copy = new CanonicalItem("Lamb");
		final CanonicalItem diff1 = new CanonicalItem("Lamb", copy);
		final CanonicalItem diff2 = new CanonicalItem("Beef");

		TestUtils.testEqualsHashcode(orig, copy, diff1, diff2);
	}
}
