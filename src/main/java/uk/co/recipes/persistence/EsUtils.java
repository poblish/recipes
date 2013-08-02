/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Client;

import com.google.common.base.Optional;
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

	@Inject
	Client esClient;

	public <T> Optional<T> findOneByIdAndType( final String inBaseUrl, final long inId, final Class<T> inIfClazz, final Class<? extends T> inImplClazz) throws JsonProcessingException, MalformedURLException, IOException {
		final Iterator<JsonNode> nodeItr = mapper.readTree( new URL( inBaseUrl + "/_search?q=id:" + inId + "&size=1") ).path("hits").path("hits").iterator();
		if (nodeItr.hasNext()) {
			return Optional.fromNullable((T) mapper.readValue( nodeItr.next().path("_source"), inImplClazz) );
		}

		return Optional.absent();
	}

	// FIXME - pretty lame!
	public <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws JsonParseException, JsonMappingException, IOException {
		final JsonNode allNodes = mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( mapper.readValue( each.path("_source"), inClass));
		}

		return allItems;
	}

    // FIXME - pretty lame!
    public <T> int countAll( final String inBaseUrl, Class<T> inClass) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits").size();
    }
}