/**
 * 
 */
package uk.co.recipes.myrrix;

import net.myrrix.common.MyrrixRecommender;
import net.myrrix.online.AbstractRescorerProvider;

import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class RecipesRescorer extends AbstractRescorerProvider {

	private static final Logger LOG = LoggerFactory.getLogger( RecipesRescorer.class );

	private final static long RECIPE_BASE_ID = 0x4000000000000000L;

	@Override
	public Rescorer<LongPair> getMostSimilarItemsRescorer( final MyrrixRecommender inRecommender, final String... inArgs) {
//		LOG.info(Arrays.toString(inArgs));

		if (inArgs == null || inArgs.length == 0) {
			return null;
		}

//		long toItemID = Long.parseLong( inArgs[0] );
//		LOG.info("toItemID = " + toItemID);

		final String desiredType = (String) inArgs[0];
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		return new Rescorer<LongPair>() {

			@Override
			public double rescore( final LongPair thing, double originalScore) {
				return originalScore;
			}

			@Override
			public boolean isFiltered( final LongPair inPair) {
				if ( isRecipe && ( inPair.getFirst() < RECIPE_BASE_ID || inPair.getSecond() < RECIPE_BASE_ID)) {
					LOG.info("RecipesRescorer: Stripping out invalid {RECIPE,RECIPE}... " + inPair);
					return true;
				}
				else if ( isItem && ( inPair.getFirst() >= RECIPE_BASE_ID || inPair.getSecond() >= RECIPE_BASE_ID)) {
					LOG.info("RecipesRescorer: Stripping out invalid {ITEM,ITEM}... " + inPair);
					return true;
				}

				return false;
			}
		};
	}
}