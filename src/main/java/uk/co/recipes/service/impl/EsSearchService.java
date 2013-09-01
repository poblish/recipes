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
import java.util.Collections;
import java.util.List;

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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

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
	    final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_SEARCHES).time();

		try
		{
			final JsonNode jn = mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + inName + "&size=9999") ).path("hits").path("hits");
	
			final List<ICanonicalItem> results = Lists.newArrayList();
	
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each.path("_source").traverse(), CanonicalItem.class) );  // FIXME Remove _source stuff where possible
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

            final JsonNode jn = mapper.readTree( new URL( recipesIndexUrl + "/_search?q=" + inName + "&size=9999") ).path("hits").path("hits");
	
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
        return findItemsByName( tagString(inTag) );
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
			return mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + inName) ).path("hits").path("hits").size();
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
			return mapper.readTree( new URL( recipesIndexUrl + "/_search?q=" + inName) ).path("hits").path("hits").size();
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

		@SuppressWarnings("unused")  // For Jackon only
		public String getName() {
			return item.getCanonicalName();
		}

		@SuppressWarnings("unused")  // For Jackon only
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

		@SuppressWarnings("unused")  // For Jackon only
		public long getId() {
			return recipe.getId();
		}

		@SuppressWarnings("unused")  // For Jackon only
		public String getTitle() {
			return recipe.getTitle();
		}

		@SuppressWarnings("unused")  // For Jackon only
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