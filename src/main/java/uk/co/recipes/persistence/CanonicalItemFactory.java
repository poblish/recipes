/**
 * 
 */
package uk.co.recipes.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";

	// FIXME - rubbish
	public static void startES() {
		ES_CLIENT = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	}

	// FIXME - rubbish
	public static void stopES() {
		ES_CLIENT.close();
	}

	public static ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
		final HttpPost req = new HttpPost( IDX_URL + "/" + inId);

		try {
			req.setEntity( new StringEntity( JacksonFactory.getMapper().writeValueAsString(inItem) ) );

			final HttpResponse resp = CLIENT.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inItem;
	}

	public static ICanonicalItem getById( String inId) throws IOException {
		return JacksonFactory.getMapper().readValue( JacksonFactory.getMapper().readTree( new URL( IDX_URL + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

	public static String toId( final String inCanonicalName) throws IOException {
		checkArgument( !inCanonicalName.contains(","), "Name should not contain comma: '" + inCanonicalName + "'");
		return inCanonicalName.toLowerCase().replace( ' ', '_');
	}

	public static Optional<ICanonicalItem> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getById( toId(inCanonicalName) ) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public static ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator) {
		return getOrCreate( inCanonicalName, inCreator, false);
	}

	public static ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator, final boolean inMatchAliases) {
		try {
			final Optional<ICanonicalItem> got = get(inCanonicalName);
	
			if (got.isPresent()) {
				return got.get();
			}

			if (inMatchAliases) {
				try {
					final SearchResponse resp = ES_CLIENT.prepareSearch("recipe").setTypes("items").setQuery( termQuery( "aliases", inCanonicalName.toLowerCase()) ).addSort( "_score", DESC).execute().actionGet();
					final SearchHit[] hits = resp.getHits().hits();

					if ( hits.length > 0) {
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
	public static Collection<CanonicalItem> listAll() throws JsonParseException, JsonMappingException, IOException {
		return EsUtils.listAll( IDX_URL, CanonicalItem.class);
	}

	public static void deleteAll() throws IOException {
		final HttpResponse resp = CLIENT.execute( new HttpDelete(IDX_URL) );
		EntityUtils.consume( resp.getEntity() );
	}
}
