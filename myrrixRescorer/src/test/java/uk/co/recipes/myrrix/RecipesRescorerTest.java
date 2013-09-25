/**
 * 
 */
package uk.co.recipes.myrrix;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;
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
    public void testRecommendRecipeFilteringNoIncludes() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, mr, new String[]{"RECIPE"});
        assertThat( rescorer.isFiltered(100L), is(true));
        assertThat( rescorer.isFiltered(3611686018427387904L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387909L), is(false));
    }

    @Test
    public void testRecommendRecipeFilteringWithIncludes() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, mr, new String[]{"RECIPE", "4611686018427387904,4611686018427387905,4611686018427387906"});
        assertThat( rescorer.isFiltered(100L), is(true));
        assertThat( rescorer.isFiltered(3611686018427387904L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(false));
        assertThat( rescorer.isFiltered(4611686018427387909L), is(true));

        final double rand = Math.random();
        assertThat( rescorer.rescore( 100L, rand), is(rand));  // Test pass-thru
    }

    @Test
    public void testRecommendItemFiltering() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, mr, new String[]{"ITEM", "1,9000,4611686018427387906"});
        assertThat( rescorer.isFiltered(1L), is(false));
        assertThat( rescorer.isFiltered(9000L), is(false));
        assertThat( rescorer.isFiltered(9001L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387906L), is(true));
    }

    @Test
    public void testSimilarityRecipeFiltering() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( mr, new String[]{"RECIPE", "4611686018427387904,4611686018427387905,4611686018427387906"});
        assertThat( rescorer.isFiltered( new LongPair(100L,100L) ), is(true));
        assertThat( rescorer.isFiltered( new LongPair(3611686018427387904L,3611686018427387904L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387904L,4611686018427387904L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387906L,4611686018427387906L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387904L,14L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(14L,4611686018427387906L)), is(true));

        final double rand = Math.random();
        assertThat( rescorer.rescore( new LongPair(100L,101L), rand), is(rand));  // Test pass-thru
    }

    @Test
    public void testSimilarityItemFiltering() {
        final MyrrixRecommender mr = mock( MyrrixRecommender.class );

        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( mr, new String[]{"ITEM", "1,9000,4611686018427387906"});
        assertThat( rescorer.isFiltered( new LongPair(100L,100L) ), is(true));
        assertThat( rescorer.isFiltered( new LongPair(3611686018427387904L,3611686018427387904L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(1L,1L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(9000L,9000L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387904L,14L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(14L,4611686018427387906L)), is(true));
    }
}