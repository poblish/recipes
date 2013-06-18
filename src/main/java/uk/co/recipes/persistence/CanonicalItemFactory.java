/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItemFactory {

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static Gson GSON = builder().create();
	private final static ObjectMapper MAPPER = new ObjectMapper();

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";


	public static ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
		final HttpPost req = new HttpPost( IDX_URL + "/" + inId);

		try {
			req.setEntity( new StringEntity( GSON.toJson(inItem) ) );
//			String s = MAPPER.writeValueAsString(inItem);
//			req.setEntity( new StringEntity( MAPPER.writeValueAsString(inItem) ) );

			final HttpResponse resp = CLIENT.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inItem;
	}

	public static ICanonicalItem get( String inId) throws IOException {
		return MAPPER.readValue( MAPPER.readTree( new URL( IDX_URL + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

	public static ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator) throws IOException {
		final String nameId = inCanonicalName.toLowerCase().replace( ' ', '_');

		try {
			final ICanonicalItem item = get(nameId);
			if ( item != null) {
				return item;
			}
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return put( inCreator.get(), nameId);
	}

	private static GsonBuilder builder() {
		final GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter( IRecipeStage.class, new InstanceCreator<IRecipeStage>() {

			@Override
			public IRecipeStage createInstance( Type type) {
				return new RecipeStage();
			}});

		gb.registerTypeAdapter( ITag.class, new InstanceCreator<ITag>() {

			@Override
			public ITag createInstance( Type type) {
				return CommonTags.SERVES_COUNT;
			}});

		return gb;
	}
}
