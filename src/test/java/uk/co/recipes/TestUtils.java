package uk.co.recipes;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Test utilities
 *
 * @author andrewr
 */
@SuppressWarnings("nls")
public final class TestUtils {

    /**
     * Pointless
     */
    private TestUtils() {
    }

    /**
     * Added for PIT-testing - a simple way to get good coverage and a high mutation score for your object
     *
     * @param inObj
     * @param inCopy
     * @param inDifferentObjects
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void testEqualsHashcode(final Object inObj, final Object inCopy, final Object... inDifferentObjects) {
        // First up, check the types match...
        assertThat((Class) inObj.getClass(), allOf(equalTo((Class) inCopy.getClass())));

        assertFalse(inObj.equals(null));  // NOPMD
        assertFalse(inObj.equals((Object) new java.util.StringTokenizer("")));
        assertThat(inObj, equalTo(inObj));
        assertThat("Copy object should not == source", inCopy, not(sameInstance(inObj)));
        assertThat("Source object should equal the Copy object", inObj, equalTo(inCopy));

        assertThat("Source.hashCode() should equal itself", inObj.hashCode(), equalTo(inObj.hashCode()));
        assertThat("Source.hashCode() should equal the Copy.hashCode()", inObj.hashCode(), equalTo(inCopy.hashCode()));

        assertEquals(inDifferentObjects.length, Sets.newHashSet(inDifferentObjects).size(), "Duplicate Different objects are present: counts differ");

        for (Object eachDiffObject : inDifferentObjects) {
            assertThat((Class) inObj.getClass(), equalTo((Class) eachDiffObject.getClass()));
            assertThat("Different object should not == source", eachDiffObject, not(sameInstance(inObj)));
            assertThat("Different object should not equal the Source/Copy object", inObj, not(equalTo(eachDiffObject)));
            assertThat("Source.hashCode() should (probably) not equal the Different.hashCode()", inObj.hashCode(), not(equalTo(eachDiffObject.hashCode()))); // (AGR) ;-)
        }
    }

    /**
     * TODO
     *
     * @param inPath
     * @return the file path
     */
    public static String getTestResPath(final String inPath) {
        // (AGR) This property should be set by the SureFire plugin, or at least by JUnit.

        String theDirPropertyToTry = "user.dir";
        if (System.getProperty("basedir") != null) {
            theDirPropertyToTry = "basedir";
        }
        final String theDirValue = System.getProperty(theDirPropertyToTry);

        Preconditions.checkNotNull(theDirValue, "{" + theDirPropertyToTry + "} system property not set");

        return theDirValue + "/src/test/resources/" + inPath;
    }

    /**
     * TODO
     *
     * @param inPath
     * @return the File object
     */
    public static File getTestResFile(final String inPath) {
        return new File(getTestResPath(inPath));
    }

    /**
     * TODO
     *
     * @param inPath
     * @return the text file contents
     */
    public static String loadTestTextFile(final String inPath) {
        try {
            return Files.toString(getTestResFile(inPath), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * TODO
     *
     * @param inPath
     * @return the binary file contents
     */
    public static byte[] loadTestBinaryFile(final String inPath) {
        try {
            return Files.toByteArray(getTestResFile(inPath));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}