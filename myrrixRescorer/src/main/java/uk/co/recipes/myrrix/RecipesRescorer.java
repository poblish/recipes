/**
 * 
 */
package uk.co.recipes.myrrix;

import java.util.Arrays;

import net.myrrix.common.MyrrixRecommender;
import net.myrrix.online.AbstractRescorerProvider;

import org.apache.mahout.cf.taste.recommender.IDRescorer;
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
	public IDRescorer getRecommendRescorer( long[] userIDs, final MyrrixRecommender recommender, final String... inArgs) {

		LOG.info("RecipesRescorer: userIDs = " + Arrays.toString(userIDs));

		final String desiredType = ( inArgs != null && inArgs.length >= 1) ? (String) inArgs[0] : "";
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		return new IDRescorer() {

			@Override
			public boolean isFiltered( final long inId) {
				if ( isRecipe && inId < RECIPE_BASE_ID) {
					LOG.trace("RecipesRescorer: Stripping out invalid {RECIPE}... " + inId);
					return true;
				}
				else if ( isItem && inId >= RECIPE_BASE_ID) {
					LOG.trace("RecipesRescorer: Stripping out invalid {ITEM}... " + inId);
					return true;
				}

				LOG.info("RecipesRescorer: PASS recommend Id " + inId);
				return false;
			}

			@Override
			public double rescore( long id, double originalScore) {
				LOG.info("RecipesRescorer: recommending " + id + " => " + originalScore);
				return originalScore;
			}
		};
	}

	@Override
	public Rescorer<LongPair> getMostSimilarItemsRescorer( final MyrrixRecommender inRecommender, final String... inArgs) {
//		LOG.info(Arrays.toString(inArgs));
//		long toItemID = Long.parseLong( inArgs[0] );
//		LOG.info("toItemID = " + toItemID);

		final String desiredType = ( inArgs != null && inArgs.length >= 1) ? (String) inArgs[0] : "";
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		return new Rescorer<LongPair>() {

			@Override
			public boolean isFiltered( final LongPair inPair) {
				if (isRecipe) {
					if ( inPair.getFirst() < RECIPE_BASE_ID || inPair.getSecond() < RECIPE_BASE_ID) {
						LOG.trace("RecipesRescorer: Stripping out invalid {RECIPE,RECIPE}... " + inPair);
						return true;
					}
				}
				else if (isItem) {
					if ( inPair.getFirst() >= RECIPE_BASE_ID || inPair.getSecond() >= RECIPE_BASE_ID) {
						LOG.trace("RecipesRescorer: Stripping out invalid {ITEM,ITEM}... " + inPair);
						return true;
					}
				}
				else if (( inPair.getFirst() >= RECIPE_BASE_ID && inPair.getSecond() < RECIPE_BASE_ID) || ( inPair.getFirst() < RECIPE_BASE_ID && inPair.getSecond() >= RECIPE_BASE_ID)) {
					LOG.trace("RecipesRescorer: Stripping out invalid {ITEM,RECIPE}... " + inPair);
					return true;
				}

				LOG.info("RecipesRescorer: PASS similarity pair " + inPair);
				return false;
			}

			@Override
			public double rescore( final LongPair inPair, double originalScore) {
				LOG.info("RecipesRescorer: similarity " + inPair + " => " + originalScore);
				return originalScore;
			}
		};
	}
}