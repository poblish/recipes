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

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class RecipesRescorer extends AbstractRescorerProvider {

	private static final Logger LOG = LoggerFactory.getLogger( RecipesRescorer.class );

	private final static long RECIPE_BASE_ID = 0x4000000000000000L;

	private static final Splitter ID_SPLITTER = Splitter.on(',');
	private static final long[] EMPTY_ARRAY = new long[0];


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

		final String desiredType = ( inArgs != null && inArgs.length >= 1) ? (String) inArgs[0] : "";
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		final long[] includeIdsSorted = ( inArgs != null) ? parseLongArrayString((String) inArgs[1]) : EMPTY_ARRAY;
		final long[] excludeIdsSorted = ( inArgs != null) ? parseLongArrayString((String) inArgs[2]) : EMPTY_ARRAY;

		return new Rescorer<LongPair>() {

			@Override
			public boolean isFiltered( final LongPair inPair) {

				if ( includeIdsSorted.length > 0 && ( !isLongInArray( includeIdsSorted, inPair.getFirst()) || !isLongInArray( includeIdsSorted, inPair.getSecond()) ) ) {
					LOG.info("RecipesRescorer: one value (" + inPair.getFirst() + "," + inPair.getSecond() + ") not in include list: " + Arrays.toString(includeIdsSorted));
					return false;
				}
				else if ( excludeIdsSorted.length > 0 && ( isLongInArray( excludeIdsSorted, inPair.getFirst()) || isLongInArray( excludeIdsSorted, inPair.getSecond()) ) ) {
					LOG.info("RecipesRescorer: one value (" + inPair.getFirst() + "," + inPair.getSecond() + ") not in exclude list: " + Arrays.toString(includeIdsSorted));
					return false;
				}

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

	private boolean isLongInArray( final long[] inArray, final long inVal) {
		return Arrays.binarySearch( inArray, inVal) >= 0;
	}

	private long[] parseLongArrayString( final String inStr) {
		if ( inStr == null || inStr.isEmpty()) {
			return EMPTY_ARRAY;
		}

		// It should go without saying that this must be a *sorted* list

		final String[] indivStrs = FluentIterable.from( ID_SPLITTER.split(inStr) ).toArray( String.class );  // Yuk!

		final long[] longs = new long[ indivStrs.length ];

		for ( int i = 0; i < indivStrs.length; i++) {
			longs[i] = Long.parseLong( indivStrs[i] );
		}

		return longs;
	}
}