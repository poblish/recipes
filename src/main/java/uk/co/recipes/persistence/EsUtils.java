/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;

import org.elasticsearch.client.Client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	public JsonParser parseSource( final String inUrlString) throws IOException {
		return mapper.readTree( new URL(inUrlString) ).path("_source").traverse();
	}

	public JsonParser parseSource( final JsonNode inJacksonNode) {
		return inJacksonNode.path("_source").traverse();
	}

	public <T> Optional<T> findOneByIdAndType( final String inBaseUrl, final long inId, final Class<T> inIfClazz, final Class<? extends T> inImplClazz) throws IOException {
		final Iterator<JsonNode> nodeItr = mapper.readTree( new URL( inBaseUrl + "/_search?q=id:" + inId + "&size=1") ).path("hits").path("hits").iterator();
		if (nodeItr.hasNext()) {
			return Optional.fromNullable((T) mapper.readValue( parseSource( nodeItr.next() ), inImplClazz) );
		}

		return Optional.absent();
	}

	// FIXME - pretty lame!
	public <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws IOException {
		final JsonNode allNodes = mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( mapper.readValue( parseSource(each), inClass));
		}

		return allItems;
	}

    public <T> long countAll( final String inBaseUrl) throws IOException {
        return mapper.readTree( new URL( inBaseUrl + "/_count") ).get("count").asLong();
    	// FIXME Throws weird errors: return esClient.prepareCount(inIndex).execute().actionGet().count();
    }
}