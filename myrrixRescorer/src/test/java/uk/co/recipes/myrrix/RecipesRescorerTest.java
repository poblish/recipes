/**
 * 
 */
package uk.co.recipes.myrrix;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import net.myrrix.common.MyrrixRecommender;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class RecipesRescorerTest {

    @Test
    public void testRecommendFiltering1() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, mr, new String[]{"RECIPE", "4611686018427387904,4611686018427387905,4611686018427387906"});
        assertThat( rescorer.isFiltered(100L), is(true));
        assertThat( rescorer.isFiltered(3611686018427387904L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(false));
        assertThat( rescorer.isFiltered(4611686018427387906L), is(false));
    }

    @Test
    public void testRecommendFiltering2() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, mr, new String[]{"ITEM", "1,9000,4611686018427387906"});
        assertThat( rescorer.isFiltered(1L), is(false));
        assertThat( rescorer.isFiltered(9000L), is(false));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387906L), is(true));
    }
}