/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsUtils {

	// FIXME - pretty lame!
	public static <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws JsonParseException, JsonMappingException, IOException {
		final JsonNode allNodes = JacksonFactory.getMapper().readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( JacksonFactory.getMapper().readValue( each.path("_source"), inClass));
		}

		return allItems;
	}
}