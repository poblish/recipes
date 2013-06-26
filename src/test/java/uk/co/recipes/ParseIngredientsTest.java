package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.tags.CommonTags.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
import uk.co.recipes.similarity.Similarity;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class ParseIngredientsTest {

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		CanonicalItemFactory.deleteAll();
		RecipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException {
		ItemsLoader.load();
	}

	@Test
	public void parseIngredients1() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=NamedItem{name=Sunflower Oil, canonical=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=Streaky Bacon, canonical=CanonicalItem{name=Streaky Bacon, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}}, notes={en=[preferably in one piece, skinned and cut into pieces, smoked]}}, Ingredient{q=900 GRAMMES, item=NamedItem{name=lamb neck fillets, canonical=CanonicalItem{name=lamb neck fillets}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=Baby Onions, canonical=CanonicalItem{name=Baby Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, notes={en=[peeled]}}, Ingredient{q=5, item=NamedItem{name=Carrot, canonical=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=button mushrooms, canonical=CanonicalItem{name=button mushrooms}}, notes={en=[small]}}, Ingredient{q=3 TBSP, item=NamedItem{name=Plain Flour, canonical=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}}, Ingredient{q=3, item=NamedItem{name=Bay Leaves, canonical=CanonicalItem{name=Bay Leaves, tags={HERB=true}}}}, Ingredient{q=SMALL BUNCHES, item=NamedItem{name=Thyme, canonical=CanonicalItem{name=Thyme, tags={HERB=true}}}}, Ingredient{q=350 ML, item=NamedItem{name=Red Wine, canonical=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}}}, Ingredient{q=350 ML, item=NamedItem{name=lamb or beef stock, canonical=CanonicalItem{name=lamb or beef stock}}}, Ingredient{q=LARGE SPLASHES, item=NamedItem{name=Worcestershire Sauce, canonical=CanonicalItem{name=Worcestershire Sauce}}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=Self-raising Flour, canonical=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}}, Ingredient{q=4 TBSP, item=NamedItem{name=Mixed Herbs, canonical=CanonicalItem{name=Mixed Herbs, tags={HERB=true}}}, notes={en=[including thyme, rosemary and parsley, chopped]}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=Butter, canonical=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}}, notes={en=[grated, chilled]}}, Ingredient{q=1, item=NamedItem{name=Lemon, canonical=CanonicalItem{name=Lemon, tags={FRUIT=true}}}, notes={en=[Juice of]}}, Ingredient{q=5, item=NamedItem{name=Bay Leaves, canonical=CanonicalItem{name=Bay Leaves, tags={HERB=true}}}}, Ingredient{q=1, item=NamedItem{name=Egg, canonical=CanonicalItem{name=Egg, tags={EGG=true}}}, notes={en=[to glaze, beaten]}}]"));
	}

	@Test
	public void parseIngredients2() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs2.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=NamedItem{name=Gnocchi, canonical=CanonicalItem{name=Gnocchi, tags={PASTA=true, ITALIAN=true}}}, notes={en=[fresh]}}, Ingredient{q=1 TBSP, item=NamedItem{name=Olive Oil, canonical=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}}, Ingredient{q=1, item=NamedItem{name=red chilli, canonical=CanonicalItem{name=red chilli}}, notes={en=[sliced, deseeded if you like]}}, Ingredient{q=1, item=NamedItem{name=medium courgette, canonical=CanonicalItem{name=medium courgette}}, notes={en=[cut into thin ribbons with a peeler]}}, Ingredient{q=4, item=NamedItem{name=Spring Onions, canonical=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, notes={en=[chopped]}}, Ingredient{q=1, item=NamedItem{name=Lemon, canonical=CanonicalItem{name=Lemon, tags={FRUIT=true}}}, notes={en=[Juice of]}}, Ingredient{q=2 HEAPED_TBSP, item=NamedItem{name=Mascarpone, canonical=CanonicalItem{name=Mascarpone, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=Parmesan, canonical=CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}}, notes={en=[(or vegetarian alternative), grated]}}, Ingredient{q=Some, item=NamedItem{name=dressed mixed leaves, canonical=CanonicalItem{name=dressed mixed leaves}}, notes={en=[to serve]}}]"));
	}

	@Test
	public void parseIngredients3() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs3.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=NamedItem{name=Onion, canonical=CanonicalItem{name=Onion, tags={VEGETABLE=true}}}, notes={en=[large]}}, Ingredient{q=6, item=NamedItem{name=Garlic Cloves, canonical=CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, notes={en=[roughly chopped]}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=Ginger, canonical=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true}}}, notes={en=[roughly chopped]}}, Ingredient{q=4 TBSP, item=NamedItem{name=Vegetable Oil, canonical=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}}, Ingredient{q=2 TSP, item=NamedItem{name=Cumin Seeds, canonical=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=Fennel Seed, canonical=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}}}, Ingredient{q=5 CM, item=NamedItem{name=Cinnamon Stick, canonical=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=Chilli Flakes, canonical=CanonicalItem{name=Chilli Flakes, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=Garam Masala, canonical=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=Turmeric, canonical=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=Caster Sugar, canonical=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}}, Ingredient{q=400 GRAMMES, item=NamedItem{name=Tomatoes, canonical=CanonicalItem{name=Tomatoes, tags={VEGETABLE=true}}}, notes={en=[can, chopped]}}, Ingredient{q=8, item=NamedItem{name=Chicken Thighs, canonical=CanonicalItem{name=Chicken Thighs, parent=CanonicalItem{name=Chicken, tags={MEAT=true}}, tags={MEAT=true}}}, notes={en=[skinned, boneless (about 800g)]}}, Ingredient{q=250 ML, item=NamedItem{name=chicken stock, canonical=CanonicalItem{name=chicken stock}}, notes={en=[hot]}}, Ingredient{q=2 TBSP, item=NamedItem{name=Coriander, canonical=CanonicalItem{name=Coriander, tags={HERB=true}}}, notes={en=[chopped]}}]"));
	}

	@Test
	public void parseIngredientsChCashBlackSpiceCurry() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 KG, item=NamedItem{name=Chicken, canonical=CanonicalItem{name=Chicken, tags={MEAT=true}}}, notes={en=[skinned]}}, Ingredient{q=6, item=NamedItem{name=Cloves, canonical=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=100 GRAMMES, item=NamedItem{name=coconut grated, canonical=CanonicalItem{name=coconut grated}}}, Ingredient{q=3 INCH, item=NamedItem{name=Cinnamon Stick, canonical=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=12, item=NamedItem{name=Garlic Cloves, canonical=CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, notes={en=[peeled, plump]}}, Ingredient{q=225 GRAMMES, item=NamedItem{name=Cashew Nuts, canonical=CanonicalItem{name=Cashew Nuts, tags={NUT=true}}}}, Ingredient{q=1, item=NamedItem{name=Onion, canonical=CanonicalItem{name=Onion, tags={VEGETABLE=true}}}, notes={en=[chopped, large]}}, Ingredient{q=0 TBSP, item=NamedItem{name=Coriander Seeds, canonical=CanonicalItem{name=Coriander Seeds, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}}, Ingredient{q=0 TSP, item=NamedItem{name=Cumin Seeds, canonical=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=4 TBSP, item=NamedItem{name=Oil, canonical=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}}}, Ingredient{q=4, item=NamedItem{name=Red Chillies, canonical=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true}}}, notes={en=[whole, dried]}}, Ingredient{q=1, item=NamedItem{name=Salt, canonical=CanonicalItem{name=Salt, tags={SALT=true}}}}]"));
	}

	@Test
	public void testSimilarity() throws IOException, IncompatibleIngredientsException {
		final List<IIngredient> ingr1 = parseIngredientsFrom("inputs.txt");
		final List<IIngredient> ingr2 = parseIngredientsFrom("inputs2.txt");
		final List<IIngredient> ingr3 = parseIngredientsFrom("inputs3.txt");
		final List<IIngredient> ingr4 = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		final List<IIngredient> ingrBol1 = parseIngredientsFrom("bol1.txt");
		final List<IIngredient> ingrBol2 = parseIngredientsFrom("bol2.txt");

		assertThat( Categorisation.forIngredients(ingr1).toString(), is("[ALCOHOL, DAIRY, EGG, FAT x 2, FLOUR x 2, FRUIT, HERB x 4, MEAT, OIL, VEGETABLE x 2, WINE]"));
		assertThat( Categorisation.forIngredients(ingr2).toString(), is("[CHEESE x 2, DAIRY x 2, FAT, FRUIT, ITALIAN x 3, OIL, PASTA, VEGETABLE]"));
		assertThat( Categorisation.forIngredients(ingr3).toString(), is("[CHILLI, FAT, HERB, INDIAN x 5, MEAT, OIL, SEED, SPICE x 6, SUGAR, VEGETABLE x 3]"));
		assertThat( Categorisation.forIngredients(ingr4).toString(), is("[CHILLI, FAT, HERB, INDIAN x 4, MEAT, NUT, OIL, SALT, SPICE x 4, VEGETABLE x 2]"));
		assertThat( Categorisation.forIngredients(ingrBol1).toString(), is("[ALCOHOL, CHEESE, DAIRY, FAT, HERB x 2, ITALIAN, MEAT, OIL, SUGAR, VEGETABLE x 4, VINEGAR, WINE]"));
		assertThat( Categorisation.forIngredients(ingrBol2).toString(), is("[DAIRY x 2, FAT, MEAT x 3, SPICE, VEGETABLE x 4]"));

		final ITag[] tags = new ITag[]{ INDIAN, CHINESE, JAPANESE, THAI, FRENCH, ITALIAN, GREEK, ENGLISH };
		assertThat( Categorisation.forIngredients(ingr1, tags).toString(), is("[]"));
		assertThat( Categorisation.forIngredients(ingr2, tags).toString(), is("[ITALIAN x 3]"));
		assertThat( Categorisation.forIngredients(ingr3, tags).toString(), is("[INDIAN x 5]"));
		assertThat( Categorisation.forIngredients(ingr4, tags).toString(), is("[INDIAN x 4]"));
		assertThat( Categorisation.forIngredients(ingrBol1, tags).toString(), is("[ITALIAN]"));
		assertThat( Categorisation.forIngredients(ingrBol2, tags).toString(), is("[]"));

		final double s12 = Similarity.amongIngredients( ingr1, ingr2);
		final double s13 = Similarity.amongIngredients( ingr1, ingr3);
		final double s23 = Similarity.amongIngredients( ingr2, ingr3);
		final double s34 = Similarity.amongIngredients( ingr3, ingr4);
		final double s1bol1 = Similarity.amongIngredients( ingr1, ingrBol1);
		final double sbol1bol2 = Similarity.amongIngredients( ingrBol1, ingrBol2);

		System.out.println(s12);
		System.out.println(s13);
		System.out.println(s23);
		System.out.println(s34);
		System.out.println(s1bol1);
		System.out.println(sbol1bol2);

		assertThat( Similarity.amongIngredients( ingr2, ingr1), is(s12));
		assertThat( Similarity.amongIngredients( ingr3, ingr1), is(s13));
		assertThat( Similarity.amongIngredients( ingr3, ingr2), is(s23));
	}

	private List<IIngredient> parseIngredientsFrom( final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/" + inFilename), Charset.forName("utf-8"))) {

			if (eachLine.startsWith("// ")) {
				continue;
			}

			final Optional<Ingredient> theIngr = IngredientParser.parse(eachLine);
			if (theIngr.isPresent()) {

				// System.out.println( JacksonFactory.getMapper().writeValueAsString( theIngr.get() ) );

				allIngredients.add( theIngr.get() );
			}
			else {
				Assert.fail(eachLine + " not matched");
			}
		}

		////////////////////////////////////////////////////////////

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredients(allIngredients);

		final Recipe r = new Recipe(inFilename);
		r.addStage(stage1);

		RecipeFactory.put( r, RecipeFactory.toId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}

	@AfterClass
	public void findGarlicRecipes() throws InterruptedException, IOException {
		Thread.sleep(1000);  // Time for indexing to happen!

		final JsonNode jn = JacksonFactory.getMapper().readTree( new URL("http://localhost:9200/recipe/recipes" + "/_search?q=name:garlic") ).path("hits").path("hits");
		assertThat( jn.size(), is(3));
	}
}
