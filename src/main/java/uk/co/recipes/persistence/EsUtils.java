/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Client;

import com.google.common.base.Throwables;
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

	// FIXME - pretty lame!
	public <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws JsonParseException, JsonMappingException, IOException {
		final JsonNode allNodes = mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( mapper.readValue( each.path("_source"), inClass));
		}

		return allItems;
	}

	/**
	 * Inspired by http://blogs.perl.org/users/clinton_gormley/2011/10/elasticsearchsequence---a-blazing-fast-ticket-server.html
	 * @param inIdForEntityType the 'type' of sequence
	 * @return a unique seqno for this 'inIdForEntityType' value
	 */
	public long getSeqnoForType( final String inIdForEntityType) {
		try {
			return esClient.prepareIndex( "sequence", "sequence").setId(inIdForEntityType).setSource("{}").execute().get().getVersion();
		}
		catch (InterruptedException e) {
			throw Throwables.propagate(e);
		}
		catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}
}