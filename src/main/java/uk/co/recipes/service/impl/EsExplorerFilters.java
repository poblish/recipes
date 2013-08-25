/**
 * 
 */
package uk.co.recipes.service.impl;

import static uk.co.recipes.metrics.MetricNames.TIMER_EXPLORER_FILTER_IDS_GET;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.IExplorerFilter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsExplorerFilters {

	private static final Logger LOG = LoggerFactory.getLogger( EsExplorerFilters.class );

	private static final IExplorerFilter NULL_FILTER = new NullFilter();

	private static final long[] EMPTY_ARRAY = new long[0];

	@Inject
	EsSearchService search;

    @Inject
    MetricRegistry metrics;


    // FIXME
	public IExplorerFilter includeExcludeTags( final ITag incl, final ITag excl) throws IOException {

		final List<ICanonicalItem> items = search.findItemsByTag(incl);
		final List<IRecipe> recipes = search.findRecipesByTag(excl);

        final long[] ids = getIdsForResults( items, recipes);

		return new IExplorerFilter() {

			@Override
			public long[] idsToInclude() {
				return ids;
			}

			@Override
			public long[] idsToExclude() {
				return EMPTY_ARRAY;
			}};
	}

	public IExplorerFilter includeTags( final ITag... inTags) throws IOException {

		final List<ICanonicalItem> items = search.findItemsByTag( inTags[0] );
		final List<IRecipe> recipes = search.findRecipesByTag( inTags[0] );

        final long[] ids = getIdsForResults( items, recipes);

		return new IExplorerFilter() {

			@Override
			public long[] idsToInclude() {
				return ids;
			}

			@Override
			public long[] idsToExclude() {
				return EMPTY_ARRAY;
			}};
	}

	public IExplorerFilter excludeTags( final ITag... inTags) throws IOException {

		final List<ICanonicalItem> items = search.findItemsByTag( inTags[0] );
		final List<IRecipe> recipes = search.findRecipesByTag( inTags[0] );

		final long[] ids = getIdsForResults( items, recipes);

		return new IExplorerFilter() {

			@Override
			public long[] idsToInclude() {
				return EMPTY_ARRAY;
			}

			@Override
			public long[] idsToExclude() {
				return ids;
			}};
	}

    private long[] getIdsForResults( final List<ICanonicalItem> inItems, final List<IRecipe> inRecipes) {
        final Timer.Context timerCtxt = metrics.timer(TIMER_EXPLORER_FILTER_IDS_GET).time();

        final long[] ids = new long[ inItems.size() + inRecipes.size()];
        int i = 0;

        for (ICanonicalItem each : inItems) {
            ids[i++] = each.getId();
        }

        for (IRecipe each : inRecipes) {
            ids[i++] = each.getId();
        }
        
        // Arrays.sort(ids);  // Do *not* bother with this. See http://bit.ly/187WEvC - we don't search enough times to make binary search worthwhile
 
        timerCtxt.close();

        LOG.info("Ids = " + Arrays.toString(ids));

        return ids;
    }

    public static IExplorerFilter nullFilter() {
    	return NULL_FILTER;
    }

	private static class NullFilter implements IExplorerFilter {

		@Override
		public long[] idsToInclude() {
			return new long[0];
		}

		@Override
		public long[] idsToExclude() {
			return new long[0];
		}
	}
}