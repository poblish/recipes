/**
 * 
 */
package uk.co.recipes.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";


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
		try {
			final Optional<ICanonicalItem> got = get(inCanonicalName);
	
			if (got.isPresent()) {
				return got.get();
			}
	
			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
