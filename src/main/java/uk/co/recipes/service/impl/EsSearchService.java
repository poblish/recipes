/**
 * 
 */
package uk.co.recipes.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.base.Throwables;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.service.api.ISearchAPI;

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
		try
		{
			final JsonNode jn = mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + inName + "&size=9999") ).path("hits").path("hits");
	
			final List<ICanonicalItem> results = Lists.newArrayList();
	
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each.path("_source"), CanonicalItem.class) );
			}
	
			return results;
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}


	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#findRecipesByName(java.lang.String)
	 */
	@Override
	public List<IRecipe> findRecipesByName( String inName) throws IOException {
		try
		{
			final JsonNode jn = mapper.readTree( new URL( recipesIndexUrl + "/_search?q=" + inName + "&size=9999") ).path("hits").path("hits");
	
			final List<IRecipe> results = Lists.newArrayList();
	
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each.path("_source"), Recipe.class) );
			}
	
			return results;
		}
		catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
		catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
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
}