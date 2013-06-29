package uk.co.recipes;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.Units;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.tags.CommonTags;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

public class IngredientsTest {

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		CanonicalItemFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException {
		ItemsLoader.load();
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException, InterruptedException {
		final ICanonicalItem lamb = CanonicalItemFactory.getOrCreate( "Lamb", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem meat = new CanonicalItem("Lamb");
				meat.addTag( CommonTags.MEAT );
				return meat;
			}});

		final ICanonicalItem lambNeck = CanonicalItemFactory.getOrCreate( "Lamb Neck", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Lamb Neck", Optional.of(lamb));
			}});

		final Ingredient lambIngredient = new Ingredient( lambNeck, new Quantity( Units.GRAMMES, 900));
		lambIngredient.addNote( ENGLISH, "Neck fillets, cut into large chunks");

		///////////////////////////////////////////////////

		final ICanonicalItem bacon = CanonicalItemFactory.getOrCreate( "Bacon", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem meat = new CanonicalItem("Bacon");
				meat.addTag( CommonTags.MEAT );
				return meat;
			}});

		final ICanonicalItem ssBacon = CanonicalItemFactory.getOrCreate( "Lamb Neck", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Smoked Streaky Bacon", Optional.of(bacon));
			}});

		final Ingredient baconIngredient = new Ingredient( ssBacon, new Quantity( Units.GRAMMES, 200));
		baconIngredient.addNote( ENGLISH, "Preferably in one piece, skinned and cut into pieces");

		///////////////////////////////////////////////////

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredient(lambIngredient);
		stage1.addIngredient(baconIngredient);
//		stage1.addIngredient(oilIngredient);

		final Recipe r = new Recipe("Herby Lamb Cobbler");
		r.addStage(stage1);
		r.addTag( CommonTags.SERVES_COUNT, "4");

		RecipeFactory.put( r, RecipeFactory.toId(r));

		///////////////////////////////////////////////////

		final ObjectMapper om = JacksonFactory.getMapper();

//		final String recipeJson = om.writeValueAsString(r);
//		System.out.println(recipeJson);
//		assertThat( recipeJson, is("{\"stages\":[{\"ingredients\":[{\"item\":{\"name\":\"Lamb Neck\",\"canonicalItem\":{\"canonicalName\":\"Lamb Neck\",\"parent\":{\"canonicalName\":\"Lamb\",\"varieties\":[],\"tags\":{\"MEAT\":true}},\"varieties\":[],\"tags\":{\"MEAT\":true}}},\"quantity\":{\"units\":\"GRAMMES\",\"number\":900},\"notes\":{\"en\":\"Neck fillets, cut into large chunks\"}},{\"item\":{\"name\":\"Lamb Neck\",\"canonicalItem\":{\"canonicalName\":\"Lamb Neck\",\"parent\":{\"canonicalName\":\"Lamb\",\"varieties\":[],\"tags\":{\"MEAT\":\"true\"}},\"varieties\":[],\"tags\":{\"MEAT\":\"true\"}}},\"quantity\":{\"units\":\"GRAMMES\",\"number\":200},\"notes\":{\"en\":\"Preferably in one piece, skinned and cut into pieces\"}},{\"item\":{\"name\":\"Sunflower Oil\",\"canonicalItem\":{\"canonicalName\":\"Sunflower Oil\",\"varieties\":[],\"tags\":{}}},\"quantity\":{\"units\":\"TBSP\",\"number\":1},\"notes\":{}}]}],\"tagsMap\":{\"SERVES_COUNT\":\"4\"}}"));

//		final Recipe retrievedRecipe = GSON.fromJson( recipeJson, Recipe.class);
//		assertThat( r, is(retrievedRecipe));

		Thread.sleep(1000);  // Time for indexing to happen!

		final JsonNode jn = om.readTree( new URL(IDX_URL + "/_search?q=MEAT:true") ).path("hits").path("hits");
		assertThat( jn.size(), is(10));

		for ( JsonNode each : jn) {
			System.out.println(om.readValue( each, CanonicalItem.class));
		}

		///////////////////////////////////////////////////

		final Map<ITag,Serializable> expectedTags = Maps.newHashMap();
		expectedTags.put( CommonTags.MEAT, true);

		assertThat( lamb.getTags(), is(expectedTags));
		assertThat( lambNeck.getTags(), is(expectedTags));
		assertThat( bacon.getTags(), is(expectedTags));
		assertThat( lambNeck.parent(), is( Optional.of(lamb) ));
		assertThat( lamb.parent().orNull(), nullValue());
	}
}