/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

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
import org.codehaus.jackson.map.ObjectMapper;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.IRecipe;

import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeFactory {

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchRecipesUrl")
	String itemIndexUrl;

	@Inject
	ObjectMapper mapper;

	@Inject
	EsUtils esUtils;


	public IRecipe put( final IRecipe inRecipe, String inId) throws IOException {
		final HttpPost req = new HttpPost( itemIndexUrl + "/" + inId);

		try {
			req.setEntity( new StringEntity( mapper.writeValueAsString(inRecipe) ) );

			final HttpResponse resp = httpClient.execute(req);
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

	// FIXME - pretty lame!
	public Collection<Recipe> listAll() throws JsonParseException, JsonMappingException, IOException {
		return esUtils.listAll( itemIndexUrl, Recipe.class);
	}

	public void deleteAll() throws IOException {
		final HttpResponse resp = httpClient.execute( new HttpDelete(itemIndexUrl) );
		EntityUtils.consume( resp.getEntity() );
	}

	/**
	 * @param items
	 * @return
	 */
	public List<IRecipe> getAll( final List<Long> inIds) {
		// TODO Auto-generated method stub
		return null;
	}
}