/**
 * 
 */
package uk.co.recipes.myrrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import net.myrrix.common.MyrrixRecommender;
import net.myrrix.online.AbstractRescorerProvider;

import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IExplorerFilterDef;
import uk.co.recipes.service.impl.DefaultExplorerFilterDef;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Longs;

import dagger.ObjectGraph;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class RecipesRescorer extends AbstractRescorerProvider {

    @Inject EsSearchService searchApi;
    @Inject EsExplorerFilters filtersApi;
    @Inject ObjectMapper mapper;

	private final JavaType itemArrayType;

	private static final Logger LOG = LoggerFactory.getLogger( RecipesRescorer.class );

	private final static long RECIPE_BASE_ID = 0x4000000000000000L;

	private static final Splitter ID_SPLITTER = Splitter.on(',');
	private static final long[] EMPTY_ARRAY = new long[0];


	public RecipesRescorer() {
	    System.out.println("Injecting dependencies...");
	    long st = System.currentTimeMillis();
        ObjectGraph.create( new RescorerModule() ).inject(this);
        System.out.println("Injecting dependencies DONE in " + ( System.currentTimeMillis() - st) + " msecs");

        itemArrayType = mapper.getTypeFactory().constructCollectionType( List.class, ICanonicalItem.class);
	}

	@Override
	public IDRescorer getRecommendToAnonymousRescorer( long[] userIDs, final MyrrixRecommender recommender, final String... inArgs) {
		return getRecommendRescorer( userIDs, recommender, inArgs);
	}

	@Override
	public IDRescorer getRecommendRescorer( long[] userIDs, final MyrrixRecommender recommender, final String... inArgs) {

		LOG.info("RECOMMEND > RecipesRescorer: userIDs = " + Arrays.toString(userIDs));

		final String desiredType = ( inArgs != null && inArgs.length >= 1) ? (String) inArgs[0] : "";
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		final FilterParameters filterParams = getIncludeExcludeArraysFromInputs("RECOMMEND", inArgs);

		return new IDRescorer() {

			@Override
			public boolean isFiltered( final long inId) {
				if ( isRecipe && inId < RECIPE_BASE_ID) {
					LOG.trace("RECOMMEND: Stripping out invalid {RECIPE}... " + inId);
					return true;
				}
				else if ( isItem && inId >= RECIPE_BASE_ID) {
					LOG.trace("RECOMMEND: Stripping out invalid {ITEM}... " + inId);
					return true;
				}

				if ( filterParams.includeIds.length > 0 && !isLongInArray( filterParams.includeIds, inId)) {
					LOG.trace("RECOMMEND: Filter out " + inId);
					return true;
				}

				// No point logging that we got here, as we already log the 'rescore' method
				return false;
			}

			@Override
			public double rescore( long id, double originalScore) {
				LOG.info("RECOMMEND: rescore " + id + " => " + originalScore);
				return originalScore;
			}
		};
	}

	@Override
	public Rescorer<LongPair> getMostSimilarItemsRescorer( final MyrrixRecommender inRecommender, final String... inArgs) {

		LOG.info("SIMILARITY > Starting");

		final String desiredType = ( inArgs != null && inArgs.length >= 1) ? (String) inArgs[0] : "";
		final boolean isRecipe = desiredType.equals("RECIPE");
		final boolean isItem = desiredType.equals("ITEM");

		final FilterParameters filterParams = getIncludeExcludeArraysFromInputs("SIMILARITY", inArgs);

		return new Rescorer<LongPair>() {

			@Override
			public boolean isFiltered( final LongPair inPair) {

				if (filterParams.mayFilterOutSelf) {
					// Exclude if either A or B aren't in Includes
					if (!includesOK( filterParams.includeIds, inPair.getFirst(), inPair.getSecond() )) {
						LOG.trace("SIMILARITY: Filter out " + inPair);
						return true;
					}
				}
				else {
					// Exclude only if incoming A (not 'current' B) isn't in Includes
					if (!includesOK( filterParams.includeIds, inPair.getFirst())) {
						LOG.trace("SIMILARITY: Filter out " + inPair);
						return true;
					}
				}

				if (!excludesOK( filterParams.excludeIds, inPair.getFirst(), inPair.getSecond() )) {
					LOG.trace("SIMILARITY: Filter out " + inPair);
					return true;
				}

				if (isRecipe) {
					if ( inPair.getFirst() < RECIPE_BASE_ID || inPair.getSecond() < RECIPE_BASE_ID) {
						LOG.trace("SIMILARITY: Stripping out invalid {RECIPE,RECIPE}... " + inPair);
						return true;
					}
				}
				else if (isItem) {
					if ( inPair.getFirst() >= RECIPE_BASE_ID || inPair.getSecond() >= RECIPE_BASE_ID) {
						LOG.trace("SIMILARITY: Stripping out invalid {ITEM,ITEM}... " + inPair);
						return true;
					}
				}
				else if (( inPair.getFirst() >= RECIPE_BASE_ID && inPair.getSecond() < RECIPE_BASE_ID) || ( inPair.getFirst() < RECIPE_BASE_ID && inPair.getSecond() >= RECIPE_BASE_ID)) {
					LOG.trace("SIMILARITY: Stripping out invalid {ITEM,RECIPE}... " + inPair);
					return true;
				}

				// No point logging that we got here, as we already log the 'rescore' method
				return false;
			}

			@Override
			public double rescore( final LongPair inPair, double originalScore) {
				LOG.info("SIMILARITY: rescore " + inPair + " => " + originalScore);
				return originalScore;
			}
		};
	}

	private boolean includesOK( final long[] inIncludes, final long... inIdsToCheck) {
		if ( inIncludes.length > 0) {
			for ( long eachLong : inIdsToCheck) {
				if (!isLongInArray( inIncludes, eachLong)) {
					if (LOG.isTraceEnabled()) {
						LOG.trace("SIMILARITY: value (" + eachLong + ") not in include list: " + Arrays.toString(inIncludes));
					}
					return false;
				}
			}
		}

		return true;
	}

	private boolean excludesOK( final long[] inExcludes, final long... inIdsToCheck) {
		if ( inExcludes.length > 0) {
			for ( long eachLong : inIdsToCheck) {
				if (isLongInArray( inExcludes, eachLong)) {
					if (LOG.isTraceEnabled()) {
						LOG.trace("SIMILARITY: value (" + eachLong + ") not in exclude list: " + Arrays.toString(inExcludes));
					}
					return false;
				}
			}
		}

		return true;
	}

	private boolean isLongInArray( final long[] inArray, final long inVal) {
	    return Longs.contains( inArray, inVal);
		// return Arrays.binarySearch( inArray, inVal) >= 0;  // Do *not* bother with this. See http://bit.ly/187WEvC - we don't search enough times to make binary search worthwhile
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

	private FilterParameters getIncludeExcludeArraysFromInputs( final String inLogLabel, final String... inArgs) {
		final FilterParameters result = new FilterParameters();

		try {
			final IExplorerFilterDef filterDef = ( inArgs != null && inArgs.length > 1 && inArgs[1] != null && inArgs[1].startsWith("{")) ? mapper.readValue( inArgs[1], DefaultExplorerFilterDef.class) : null;
			if ( filterDef != null) {
				LOG.info( inLogLabel + ": Got Filter: " + filterDef);
				final IExplorerFilter filter = filtersApi.from(filterDef);
	
				result.includeIds = filter.idsToInclude();
				result.excludeIds = filter.idsToExclude();
				result.mayFilterOutSelf = false;
			}
			else {
				@SuppressWarnings("unchecked")
				final List<ICanonicalItem> itemsList = (List<ICanonicalItem>) (( inArgs != null && inArgs.length > 2 && inArgs[2] != null && inArgs[2].startsWith("[")) ? mapper.readValue( inArgs[2], itemArrayType) : null);
				if ( itemsList != null) {
					LOG.info( inLogLabel + ": Find Recipes, with Filter itemsList: " + itemsList);

					final List<IRecipe> recipesToInclude = searchApi.findRecipesByItemName( Iterables.toArray( itemsList, ICanonicalItem.class));
					if ( recipesToInclude.isEmpty()) {
						result.includeIds = new long[]{-1L};
						return result;  // Nothing to do, bail out with a zero result
					}

					int i = 0;
					result.includeIds = new long[ recipesToInclude.size() ];

		            for ( IRecipe each : recipesToInclude) {
		            	result.includeIds[i++] = each.getId();
		            }
		
		            // No excludeIds
				}
				else {
					LOG.info( inLogLabel + ": No FilterDef or Items list, using Args: " + Arrays.toString(inArgs));
					result.includeIds = ( inArgs != null && inArgs.length > 1) ? parseLongArrayString(inArgs[1]) : EMPTY_ARRAY;
					result.excludeIds = ( inArgs != null && inArgs.length > 2) ? parseLongArrayString(inArgs[2]) : EMPTY_ARRAY;
				}
			}

			return result;
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private static class FilterParameters {
		long[] includeIds = EMPTY_ARRAY;
		long[] excludeIds = EMPTY_ARRAY;
		boolean mayFilterOutSelf = true; // Why would this *ever* be true?
	}
}