/**
 * 
 */
package uk.co.recipes.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.ICanonicalItem;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItemFactory {

	private static Client ES_CLIENT;

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchItemsUrl")
	String itemIndexUrl;

	// FIXME - rubbish
	public static void startES() {
		ES_CLIENT = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	}

	// FIXME - rubbish
	public static void stopES() {
		ES_CLIENT.close();
	}

	public ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
		final HttpPost req = new HttpPost( itemIndexUrl + "/" + inId);

		try {
			req.setEntity( new StringEntity( JacksonFactory.getMapper().writeValueAsString(inItem) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inItem;
	}

	public ICanonicalItem getById( String inId) throws IOException {
		return JacksonFactory.getMapper().readValue( JacksonFactory.getMapper().readTree( new URL( itemIndexUrl + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

	public static String toId( final String inCanonicalName) throws IOException {
		checkArgument( !inCanonicalName.contains(","), "Name should not contain comma: '" + inCanonicalName + "'");
		return inCanonicalName.toLowerCase().replace( ' ', '_');
	}

	public Optional<ICanonicalItem> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getById( toId(inCanonicalName) ) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator) {
		return getOrCreate( inCanonicalName, inCreator, false);
	}

	public ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator, final boolean inMatchAliases) {
		try {
			final Optional<ICanonicalItem> got = get(inCanonicalName);
	
			if (got.isPresent()) {
				return got.get();
			}

			if (inMatchAliases) {
				try {
					// http://www.elasticsearch.org/guide/reference/query-dsl/match-query/
					final SearchResponse resp = ES_CLIENT.prepareSearch("recipe").setTypes("items").setQuery( matchPhraseQuery( "aliases", inCanonicalName.toLowerCase()) ).addSort( "_score", DESC).execute().actionGet();
					final SearchHit[] hits = resp.getHits().hits();

					if ( /* Yes, want only one great match */ hits.length == 1) {
						final ICanonicalItem mappedAlias = JacksonFactory.getMapper().readValue( hits[0].getSourceAsString(), CanonicalItem.class);
						if ( mappedAlias != null) {
							System.out.println("Successfully mapped Alias '" + inCanonicalName + "' => " + mappedAlias);
							return mappedAlias;
						}
					}
				}
				catch (IOException e) { /* e.printStackTrace(); */ }
			}

			System.out.println("Creating '" + inCanonicalName + "' ...");

			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	// FIXME - pretty lame!
	public Collection<CanonicalItem> listAll() throws JsonParseException, JsonMappingException, IOException {
		return EsUtils.listAll( itemIndexUrl, CanonicalItem.class);
	}

	public void deleteAll() throws IOException {
		final HttpResponse resp = httpClient.execute( new HttpDelete(itemIndexUrl) );
		EntityUtils.consume( resp.getEntity() );
	}
}