/**
 * 
 */
package uk.co.recipes.service.impl;

import static uk.co.recipes.metrics.MetricNames.TIMER_ITEMS_SEARCHES;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_SEARCHES;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.common.base.Throwables;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.ISearchAPI;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSearchService implements ISearchAPI {

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
}