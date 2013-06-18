package uk.co.recipes;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.CanonicalItemFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.gson.Gson;

public class IngredientsTest {

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static Gson GSON = new Gson();
	private final static String	IDX_URL = "http://localhost:9200/recipe/items";


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		final HttpResponse resp = CLIENT.execute( new HttpDelete(IDX_URL) );
		EntityUtils.consume( resp.getEntity() );
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException {
		final ICanonicalItem lamb = CanonicalItemFactory.getOrCreate( "Lamb", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Lamb");
			}});

		final ICanonicalItem lambNeck = CanonicalItemFactory.getOrCreate( "Lamb Neck", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Lamb Neck", lamb);
			}});

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
}