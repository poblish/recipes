/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
import uk.co.recipes.api.ICanonicalItem;
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


	/* (non-Javadoc)
	 * @see uk.co.recipes.service.api.ISearchAPI#findItemsByName(java.lang.String)
	 */
	@Override
	public List<ICanonicalItem> findItemsByName( String inName) throws IOException {
		try
		{
			final JsonNode jn = mapper.readTree( new URL( itemIndexUrl + "/_search?q=" + inName) ).path("hits").path("hits");
			assertThat( jn.size(), is(10));
	
			final List<ICanonicalItem> results = Lists.newArrayList();
	
			for ( final JsonNode each : jn) {
				results.add( mapper.readValue( each, CanonicalItem.class) );
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
}