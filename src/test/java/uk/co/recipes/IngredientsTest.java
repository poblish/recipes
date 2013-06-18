package uk.co.recipes;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.Units;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class IngredientsTest {

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static Gson GSON = builder().create();
	private final static ObjectMapper MAPPER = new ObjectMapper();

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		final HttpResponse resp = CLIENT.execute( new HttpDelete(IDX_URL) );
		EntityUtils.consume( resp.getEntity() );

		final ICanonicalItem lamb = new CanonicalItem("Lamb");
		final ICanonicalItem lambNeck = new CanonicalItem("Lamb Neck", lamb);

		put(lamb, "1");
		put(lambNeck, "2");
	}

	public void put( final ICanonicalItem inItem, String inId) throws IOException {
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
	}

	public ICanonicalItem get( final ICanonicalItem inItem, String inId) throws IOException {
		return MAPPER.readValue( MAPPER.readTree( new URL( IDX_URL + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException {
		final ICanonicalItem lamb = get( null, "1");
		final ICanonicalItem lambNeck = get( null, "2"); // new CanonicalItem("Lamb Neck", lamb);

		final Ingredient lambIngredient = new Ingredient( new NamedItem(lambNeck), new Quantity( Units.GRAMMES, 900));
		lambIngredient.addNote( ENGLISH, "Neck fillets, cut into large chunks");

		///////////////////////////////////////////////////

		final ICanonicalItem bacon = new CanonicalItem("Bacon");
		final ICanonicalItem ssBacon = new CanonicalItem("Smoked Streaky Bacon", bacon);

		final Ingredient baconIngredient = new Ingredient( new NamedItem(ssBacon), new Quantity( Units.GRAMMES, 200));
		baconIngredient.addNote( ENGLISH, "Preferably in one piece, skinned and cut into pieces");

		///////////////////////////////////////////////////

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredient(lambIngredient);
		stage1.addIngredient(baconIngredient);

		final Recipe r = new Recipe();
		r.addStage(stage1);
		r.addTag( CommonTags.SERVES_COUNT, "4");

		///////////////////////////////////////////////////

		final String recipeJson = GSON.toJson(r);
		System.out.println(recipeJson);
		assertThat( recipeJson, is("{\"stages\":[{\"ingredients\":[{\"item\":{\"name\":\"Lamb Neck\",\"canonicalItem\":{\"canonicalName\":\"Lamb Neck\",\"parent\":{\"canonicalName\":\"Lamb\",\"varieties\":[]},\"varieties\":[]}},\"quantity\":{\"units\":\"GRAMMES\",\"number\":900},\"notes\":{\"en\":\"Neck fillets, cut into large chunks\"}},{\"item\":{\"name\":\"Smoked Streaky Bacon\",\"canonicalItem\":{\"canonicalName\":\"Smoked Streaky Bacon\",\"parent\":{\"canonicalName\":\"Bacon\",\"varieties\":[]},\"varieties\":[]}},\"quantity\":{\"units\":\"GRAMMES\",\"number\":200},\"notes\":{\"en\":\"Preferably in one piece, skinned and cut into pieces\"}}]}],\"tagsMap\":{\"SERVES_COUNT\":\"4\"}}"));

//		final Recipe retrievedRecipe = GSON.fromJson( recipeJson, Recipe.class);
//		assertThat( r, is(retrievedRecipe));

		///////////////////////////////////////////////////

		assertThat( lambNeck.parent(), is( Optional.of(lamb) ));
		assertThat( lamb.parent().orNull(), nullValue());
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