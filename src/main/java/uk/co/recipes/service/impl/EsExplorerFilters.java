/**
 * 
 */
package uk.co.recipes.service.impl;

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

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsExplorerFilters {

	private static final Logger LOG = LoggerFactory.getLogger( EsExplorerFilters.class );

	private static final long[] EMPTY_ARRAY = new long[0];

	@Inject
	EsSearchService search;


	public IExplorerFilter includeTags( final ITag... inTags) throws IOException {

		final List<ICanonicalItem> items = search.findItemsByTag( inTags[0] );
		final List<IRecipe> recipes = search.findRecipesByTag( inTags[0] );

		final long[] ids = new long[ items.size() + recipes.size()];
		int i = 0;

		for ( ICanonicalItem each : items) {
			ids[i++] = each.getId();
		}

		for ( IRecipe each : recipes) {
			ids[i++] = each.getId();
		}
 
		Arrays.sort(ids);
 
		LOG.info("Ids = " + Arrays.toString(ids));

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

		final long[] ids = new long[ items.size() + recipes.size()];
		int i = 0;

		for ( ICanonicalItem each : items) {
			ids[i++] = each.getId();
		}

		for ( IRecipe each : recipes) {
			ids[i++] = each.getId();
		}
 
		Arrays.sort(ids);
 
		LOG.info("Ids = " + Arrays.toString(ids));

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
}