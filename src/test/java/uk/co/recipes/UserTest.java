/**
 * 
 */
package uk.co.recipes;

import org.testng.annotations.Test;

import uk.co.recipes.api.IUser;


/**
 * @author andrewr
 *
 */
public class UserTest {

    @Test
    public void testEqualsHash() {
        final IUser u1 = new User( "aregan", "Andrew Regan");
        final IUser u2 = new User( "aregan", "Andrew Regan");
        final IUser u3 = new User( "aregan", "XXX");
        final IUser u4 = new User( "xxx", "Andrew Regan");
        final IUser u5 = new User( "aregan", "Andrew Regan");
        u5.setId(1981L);

        TestUtils.testEqualsHashcode(u1, u2, u3, u4, u5);
    }
}