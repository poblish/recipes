/**
 * 
 */
package uk.co.recipes.service.impl;

import static uk.co.recipes.metrics.MetricNames.TIMER_EXPLORER_FILTER_IDS_GET;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.IExplorerFilter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

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


    public Builder build() {
    	return new Builder();
    }

    public class Builder {

    	private long[] includeIds = EMPTY_ARRAY;
    	private long[] excludeIds = EMPTY_ARRAY;

    	public Builder includeTag( final ITag inTag) throws IOException {

    		final List<ICanonicalItem> items = search.findItemsByTag(inTag);
    		final List<IRecipe> recipes = search.findRecipesByTag(inTag);
    		final long[] newIds = getIdsForResults( items, recipes);

    		if ( includeIds.length == 0) {
    		    includeIds = newIds;
    		}
    		else {
        		// INTERSECTION: Include things in all of the categories
        		final Set<Long> union = Sets.newHashSet( Longs.asList(includeIds) );
        		union.retainAll( Longs.asList(newIds) );
    
        		includeIds = new long[ union.size() ];
                int i = 0;
    
                for ( Long each : union) {
                    includeIds[i++] = each;
                }
    		}

            return this;
    	}

    	// FIXME - very inefficient
    	public Builder includeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			includeTag(each);
    		}
            return this;
    	}

    	// FIXME - very inefficient
    	public Builder includeTags( final Collection<ITag> inTags) throws IOException {
    		for ( ITag each : inTags) {
    			includeTag(each);
    		}
            return this;
    	}

    	public Builder excludeTag( final ITag inTag) throws IOException {

    		final List<ICanonicalItem> items = search.findItemsByTag(inTag);
    		final List<IRecipe> recipes = search.findRecipesByTag(inTag);
            excludeIds = Longs.concat( excludeIds, getIdsForResults( items, recipes));  // UNION: Exclude anything in any of the categories
            return this;
    	}

    	// FIXME - very inefficient
    	public Builder excludeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			excludeTag(each);
    		}
            return this;
    	}

    	// FIXME - very inefficient
    	public Builder excludeTags( final Collection<ITag> inTags) throws IOException {
    		for ( ITag each : inTags) {
    			excludeTag(each);
    		}
            return this;
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

    	public IExplorerFilter toFilter() {
    		return new IExplorerFilter() {

				@Override
				public long[] idsToInclude() {
					return includeIds;
				}

				@Override
				public long[] idsToExclude() {
					return excludeIds;
				}};
    	}
    }

    public static IExplorerFilter nullFilter() {
    	return NULL_FILTER;
    }

	private static class NullFilter implements IExplorerFilter {

		@Override
		public long[] idsToInclude() {
			return EMPTY_ARRAY;
		}

		@Override
		public long[] idsToExclude() {
			return EMPTY_ARRAY;
		}
	}
}