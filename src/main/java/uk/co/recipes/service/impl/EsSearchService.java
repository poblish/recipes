/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static uk.co.recipes.metrics.MetricNames.TIMER_ITEMS_SEARCHES;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_SEARCHES;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Throwables;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.tags.TagUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSearchService implements ISearchAPI {

    @Inject
    Client esClient;

	@Inject
	ObjectMapper mapper;

	@Inject
	MetricRegistry metrics;

	@Inject
	@Named("elasticSearchItemsUrl")
	String itemIndexUrl;

	@Inject
	@Named("elasticSearchRecipesUrl")
	String recipesIndexUrl;


	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#findItemsByName(java.lang.String)
	 */
	@Override
	public List<ICanonicalItem> findItemsByName( String inName) throws IOException {
	    return findItemsByName( inName, false);
	}

	private List<ICanonicalItem> findItemsByName( String inName, boolean inSortByName) throws IOException {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_SEARCHES).time();

		try
		{
/*			FIXME No, I don't know why this doesn't work...

			if (inSortByName) {
				final SearchHit[] sortedHits = esClient.prepareSearch("recipe").setTypes("items").setSize(9999).setQuery( multiMatchQuery( "tags", inName) ).addSort( "canonicalName", SortOrder.ASC).setTrackScores(false).execute().get().getHits().hits();

				for ( SearchHit each : sortedHits) {
					results.add( mapper.readValue( each.getSourceAsString(), CanonicalItem.class) );
				}
			} */

			final JsonNode jn = mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + URLEncoder.encode( inName, "utf-8") + "&size=9999") ).path("hits").path("hits");
	
 			final List<ICanonicalItem> results = Lists.newArrayList();
			
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each.path("_source").traverse(), CanonicalItem.class) );  // FIXME Remove _source stuff where possible
			}

			// Yuk FIXME by letting ES do this right!
			if (inSortByName) {
				Collections.sort( results, new Comparator<ICanonicalItem>() {
	
					@Override
					public int compare( ICanonicalItem o1, ICanonicalItem o2) {
						return o1.getCanonicalName().compareToIgnoreCase( o2.getCanonicalName() );
					}} );
			}

			return results;
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
        finally {
            timerCtxt.stop();
        }
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#findRecipesByName(java.lang.String)
	 */
	@Override
	public List<IRecipe> findRecipesByName( String inName) throws IOException {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_SEARCHES).time();

		try
		{
            // esClient.prepareSearch("recipe").setTypes("items").setQuery("ginger").setSize(50).execute().get().getHits().toXContent( JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS);

            final JsonNode jn = mapper.readTree( new URL( recipesIndexUrl + "/_search?q=" + URLEncoder.encode( inName, "utf-8") + "&size=9999") ).path("hits").path("hits");
	
			final List<IRecipe> results = Lists.newArrayList();
	
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each.path("_source").traverse(), Recipe.class) );  // FIXME Remove _source stuff where possible
			}
	
			return results;
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
        finally {
            timerCtxt.stop();
        }
	}

    @Override
    public List<ICanonicalItem> findItemsByTag( final ITag inTag) throws IOException {
        return findItemsByName( tagString(inTag), true);
    }

    @Override
    public List<IRecipe> findRecipesByTag( final ITag inTag) throws IOException {
        return findRecipesByName( tagString(inTag) );
    }

    /* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#countItemsByName(java.lang.String)
	 */
	@Override
	public int countItemsByName( String inName) throws IOException {
		try
		{
			return mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + URLEncoder.encode( inName, "utf-8")) ).path("hits").path("hits").size();
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#countRecipesByName(java.lang.String)
	 */
	@Override
	public int countRecipesByName( String inName) throws IOException {
		try
		{
			return mapper.readTree( new URL( recipesIndexUrl + "/_search?q=" + URLEncoder.encode( inName, "utf-8")) ).path("hits").path("hits").size();
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}

    @Override
    public int countItemsByTag( final ITag inTag) throws IOException {
        return countItemsByName( tagString(inTag) );
    }

    @Override
    public int countRecipesByTag( final ITag inTag) throws IOException {
        return countRecipesByName( tagString(inTag) );
    }

    private String tagString( final ITag inTag) {
        return inTag + ":true";
    }

	@Override
	public List<ITag> findTagsByName( String inName) throws IOException {
		// A bit of a cheat...
		return TagUtils.findTagsByName(inName);
	}

	@Override
	public int countTagsByName( String inName) throws IOException {
		return findTagsByName(inName).size();
	}

	@Override
	public List<IRecipe> findRecipesByItemName( final ICanonicalItem... inItems) throws IOException {
		Set<String> cNames = Sets.newHashSet();  // FIXME Factor this out somewhere

		for ( ICanonicalItem each : inItems) {
			cNames.add( each.getCanonicalName() );
		}

		return findRecipesByItemName( Iterables.toArray( cNames, String.class));
	}

	@Override
	public List<IRecipe> findRecipesByItemName( String... inNames) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countRecipesByItemName( String... inNames) throws IOException {
		return findRecipesByItemName(inNames).size();  // FIXME, lame
	}

    @Override
    public List<ISearchResult<?>> findPartial( final String inStr) throws IOException {
    	return findPartial( inStr, 10);
    }

    @Override
    public List<ISearchResult<?>> findPartial( final String inStr, final int inSize) throws IOException {
        final SearchResponse resp = esClient.prepareSearch("recipe").setTypes("items","recipes").setQuery( matchPhraseQuery( "autoCompleteTerms", inStr) ).setSize(inSize)/* .addSort( "_score", DESC) */.execute().actionGet();
        final SearchHit[] hits = resp.getHits().hits();

        if ( hits.length == 0) {
            return Collections.emptyList();
        }

        final List<ISearchResult<?>> results = Lists.newArrayList();

        for ( final SearchHit eachHit : hits) {
            if ( eachHit.getType().equals("items")) {
                results.add( new ItemSearchResult( mapper.readValue( eachHit.getSourceAsString(), CanonicalItem.class) ));
            }
            else {
                results.add( new RecipeSearchResult( mapper.readValue( eachHit.getSourceAsString(), Recipe.class) ));
            }
        }
        return results;
    }

    private static class ItemSearchResult implements ISearchResult<ICanonicalItem> {

		private ICanonicalItem item;

		public ItemSearchResult( final ICanonicalItem item) {
			this.item = item;
		}

		public long getId() {
			return item.getId();
		}

		@Override
		public String getDisplayName() {
			return item.getCanonicalName();
		}

		public String getType() {
			return "item";
		}

		@JsonIgnore
		@Override
		public ICanonicalItem getEntity() {
			return item;
		}

		public String toString() {
			return Objects.toStringHelper(this).add( "itemName", getEntity().getCanonicalName()).toString();
		}
    }

    private static class RecipeSearchResult implements ISearchResult<IRecipe> {

		private IRecipe recipe;

		public RecipeSearchResult( final IRecipe recipe) {
			this.recipe = recipe;
		}

		public long getId() {
			return recipe.getId();
		}

		@Override
		public String getDisplayName() {
			return recipe.getTitle();
		}

		public String getType() {
			return "recipe";
		}

		@JsonIgnore
		@Override
		public IRecipe getEntity() {
			return recipe;
		}

		public String toString() {
			return Objects.toStringHelper(this).add( "recipeName", getEntity().getTitle()).toString();
		}
    }
}