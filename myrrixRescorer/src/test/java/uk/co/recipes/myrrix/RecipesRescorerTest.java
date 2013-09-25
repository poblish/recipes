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

    private final static MyrrixRecommender REC = mock( MyrrixRecommender.class );

    @Test
    public void testRecommendRecipeFilteringNoIncludes() {
        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, REC, new String[]{"RECIPE"});
        assertThat( rescorer.isFiltered(100L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387909L), is(false));
    }

    @Test
    public void testRecommendRecipeFilteringWithIncludes() {
        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, REC, new String[]{"RECIPE", "4611686018427387904,4611686018427387905,4611686018427387906"});
        assertThat( rescorer.isFiltered(100L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(false));  // An include
        assertThat( rescorer.isFiltered(4611686018427387909L), is(true));  // Good recipe, but not an include

        final double rand = Math.random();
        assertThat( rescorer.rescore( 100L, rand), is(rand));  // Test pass-thru
    }

    @Test
    public void testRecommendItemFiltering() {
        final MyrrixRecommender REC = mock( MyrrixRecommender.class );

        final IDRescorer rescorer = new RecipesRescorer().getRecommendRescorer( new long[]{1L}, REC, new String[]{"ITEM", "1,9000,4611686018427387906"});
        assertThat( rescorer.isFiltered(1L), is(false));
        assertThat( rescorer.isFiltered(9000L), is(false));
        assertThat( rescorer.isFiltered(9001L), is(true));
        assertThat( rescorer.isFiltered(4611686018427387904L), is(true));  // Filtered, because it's a Recipe Id
        assertThat( rescorer.isFiltered(4611686018427387906L), is(true));  // Filtered, because it's a Recipe Id, even though include ^ wrongly includes it
    }

    @Test
    public void testSimilarityRecipeFiltering() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{"RECIPE", "4611686018427387904,4611686018427387905"});
        assertThat( rescorer.isFiltered( new LongPair(100L,100L) ), is(true));  // Not Recipe
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387904L,14L)), is(true));  // One is not a Recipe
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387905L,4611686018427387905L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(5611686018427387904L,5611686018427387904L)), is(true));  // A Recipe, but not in include

        final double rand = Math.random();
        assertThat( rescorer.rescore( new LongPair(100L,101L), rand), is(rand));  // Test pass-thru
    }

    @Test
    public void testSimilarityRecipeFilteringNulls1() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{"RECIPE", null, null});
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387907L,4611686018427387907L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(14L,4611686018427387906L)), is(true));  // Invalid combination
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387906L,14L)), is(true));  // Invalid combination
    }

    @Test
    public void testSimilarityRecipeFilteringNulls2() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, (String[]) null);
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387906L,4611686018427387906L)), is(false));
    }

    @Test
    public void testSimilarityItemFiltering() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{"ITEM", "1,9000,4611686018427387906"});
        assertThat( rescorer.isFiltered( new LongPair(100L,100L) ), is(true));
        assertThat( rescorer.isFiltered( new LongPair(3611686018427387904L,3611686018427387904L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(1L,1L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(9000L,9000L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387904L,14L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(14L,4611686018427387906L)), is(true));
    }

    @Test
    public void testSimilarityItemFilteringInclExcl() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{"ITEM", "", "3,4"});
        assertThat( rescorer.isFiltered( new LongPair(1L,1L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(9000L,9000L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(2L,2L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(3L,3L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(4L,4L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(5L,5L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(1L,4611686018427387906L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387906L,4611686018427387906L)), is(true));
    }

    @Test
    public void testSimilarityItemFilteringNulls() {
        final Rescorer<LongPair> rescorer = new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{"ITEM", null, null});
        assertThat( rescorer.isFiltered( new LongPair(1L,1L)), is(false));
    }

    @Test
    public void testSimilarityAll1() {
    	doTestSimilarityAll( new RecipesRescorer().getMostSimilarItemsRescorer( REC, new String[]{}));
    }

    @Test
    public void testSimilarityAll2() {
    	doTestSimilarityAll( new RecipesRescorer().getMostSimilarItemsRescorer( REC, (String[]) null));
    }

    private void doTestSimilarityAll( Rescorer<LongPair> rescorer) {
        assertThat( rescorer.isFiltered( new LongPair(1L,1L)), is(false));
        assertThat( rescorer.isFiltered( new LongPair(1L,4611686018427387906L)), is(true));
        assertThat( rescorer.isFiltered( new LongPair(4611686018427387906L,1L)), is(true));
    }

    @Test
    public void testRecommendationsAll1() {
    	doTestRecommendationsAll( new RecipesRescorer().getRecommendRescorer( new long[]{1L}, REC, new String[]{}));
    }

    @Test
    public void testRecommendationsAll2() {
    	doTestRecommendationsAll( new RecipesRescorer().getRecommendRescorer( new long[]{1L}, REC, (String[]) null));
    }

	private void doTestRecommendationsAll( IDRescorer rescorer) {
        assertThat( rescorer.isFiltered(1L), is(false));
	}
}