/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsUtils {

	@Inject
	ObjectMapper mapper;

	// FIXME - pretty lame!
	public <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws JsonParseException, JsonMappingException, IOException {
		final JsonNode allNodes = mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( mapper.readValue( each.path("_source"), inClass));
		}

		return allItems;
	}
}