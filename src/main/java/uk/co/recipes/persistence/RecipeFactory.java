/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import uk.co.recipes.api.IRecipe;

import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeFactory {

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static String	IDX_URL = "http://localhost:9200/recipe/recipes";


	public static IRecipe put( final IRecipe inRecipe, String inId) throws IOException {
		final HttpPost req = new HttpPost( IDX_URL + "/" + inId);

		try {
			req.setEntity( new StringEntity( JacksonFactory.getMapper().writeValueAsString(inRecipe) ) );

			final HttpResponse resp = CLIENT.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), isOneOf(201, 200));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inRecipe;
	}

	public static String toId( final IRecipe inRecipe) throws IOException {
		return inRecipe.getTitle().toLowerCase().replace( ' ', '_');
	}
}