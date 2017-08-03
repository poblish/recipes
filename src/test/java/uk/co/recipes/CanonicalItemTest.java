/**
 *
 */
package uk.co.recipes;

import com.google.common.base.Optional;
import org.testng.annotations.Test;
import uk.co.recipes.api.ICanonicalItem;

/**
 * TODO
 *
 * @author andrewregan
 */
public class CanonicalItemTest {

    @Test
    public void testEqualsHash() {
        final ICanonicalItem orig = new CanonicalItem("Lamb");
        final ICanonicalItem copy = new CanonicalItem("Lamb");
        final ICanonicalItem diff1 = new CanonicalItem("Lamb", Optional.of(copy));
        final ICanonicalItem diff2 = new CanonicalItem("Beef");

        TestUtils.testEqualsHashcode(orig, copy, diff1, diff2);
    }
}
