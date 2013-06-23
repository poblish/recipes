package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=NamedItem{name=sunflower oil, canonical=CanonicalItem{name=sunflower oil, tags={OIL=true}}}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=smoked streaky bacon, canonical=CanonicalItem{name=smoked streaky bacon}}, notes={en=preferably in one piece, skinned and cut into pieces}}, Ingredient{q=900 GRAMMES, item=NamedItem{name=lamb neck fillets, canonical=CanonicalItem{name=lamb neck fillets}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=baby onions, canonical=CanonicalItem{name=baby onions}}, notes={en=peeled}}, Ingredient{q=5, item=NamedItem{name=carrot, canonical=CanonicalItem{name=carrot}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=small button mushrooms, canonical=CanonicalItem{name=small button mushrooms}}}, Ingredient{q=3 TBSP, item=NamedItem{name=plain flour, canonical=CanonicalItem{name=plain flour}}}, Ingredient{q=3, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=SMALL BUNCHES, item=NamedItem{name=thyme, canonical=CanonicalItem{name=thyme}}}, Ingredient{q=350 ML, item=NamedItem{name=red wine, canonical=CanonicalItem{name=red wine}}}, Ingredient{q=350 ML, item=NamedItem{name=lamb or beef stock, canonical=CanonicalItem{name=lamb or beef stock}}}, Ingredient{q=LARGE SPLASHES, item=NamedItem{name=Worcestershire sauce, canonical=CanonicalItem{name=Worcestershire sauce}}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=self-raising flour, canonical=CanonicalItem{name=self-raising flour}}}, Ingredient{q=4 TBSP, item=NamedItem{name=chopped mixed herbs, canonical=CanonicalItem{name=chopped mixed herbs}}, notes={en=including thyme, rosemary and parsley}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=chilled butter, canonical=CanonicalItem{name=chilled butter}}, notes={en=grated}}, Ingredient{q=1, item=NamedItem{name=lemon, canonical=CanonicalItem{name=lemon}}, notes={en=Juice of}}, Ingredient{q=5, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=1, item=NamedItem{name=beaten egg, canonical=CanonicalItem{name=beaten egg}}, notes={en=to glaze}}]"));
	}

	@Test
	public void parseIngredients2() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs2.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=NamedItem{name=fresh gnocchi, canonical=CanonicalItem{name=fresh gnocchi}}}, Ingredient{q=1 TBSP, item=NamedItem{name=olive oil, canonical=CanonicalItem{name=olive oil, tags={OIL=true}}}}, Ingredient{q=1, item=NamedItem{name=red chilli, canonical=CanonicalItem{name=red chilli}}, notes={en=sliced, deseeded if you like}}, Ingredient{q=1, item=NamedItem{name=medium courgette, canonical=CanonicalItem{name=medium courgette}}, notes={en=cut into thin ribbons with a peeler}}, Ingredient{q=4, item=NamedItem{name=spring onions, canonical=CanonicalItem{name=spring onions}}, notes={en=chopped}}, Ingredient{q=1, item=NamedItem{name=lemon, canonical=CanonicalItem{name=lemon}}, notes={en=Juice of}}, Ingredient{q=2 HEAPED_TBSP, item=NamedItem{name=mascarpone, canonical=CanonicalItem{name=mascarpone}}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=parmesan, canonical=CanonicalItem{name=parmesan}}, notes={en=(or vegetarian alternative), grated}}, Ingredient{q=Some, item=NamedItem{name=dressed mixed leaves, canonical=CanonicalItem{name=dressed mixed leaves}}, notes={en=to serve}}]"));
	}

	@Test
	public void parseIngredients3() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs3.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=NamedItem{name=large onion, canonical=CanonicalItem{name=large onion}}}, Ingredient{q=6, item=NamedItem{name=garlic cloves, canonical=CanonicalItem{name=garlic cloves}}, notes={en=roughly chopped}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=Ginger, canonical=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true}}}, notes={en=roughly chopped}}, Ingredient{q=4 TBSP, item=NamedItem{name=vegetable oil, canonical=CanonicalItem{name=vegetable oil, tags={OIL=true}}}}, Ingredient{q=2 TSP, item=NamedItem{name=Cumin Seeds, canonical=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=fennel seed, canonical=CanonicalItem{name=fennel seed, tags={SPICE=true}}}}, Ingredient{q=5 CM, item=NamedItem{name=Cinnamon Stick, canonical=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=chilli flakes, canonical=CanonicalItem{name=chilli flakes}}}, Ingredient{q=1 TSP, item=NamedItem{name=garam masala, canonical=CanonicalItem{name=garam masala}}}, Ingredient{q=1 TSP, item=NamedItem{name=Turmeric, canonical=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=caster sugar, canonical=CanonicalItem{name=caster sugar}}}, Ingredient{q=400 GRAMMES, item=NamedItem{name=can chopped tomatoes, canonical=CanonicalItem{name=can chopped tomatoes}}}, Ingredient{q=8, item=NamedItem{name=chicken thighs, canonical=CanonicalItem{name=chicken thighs}}, notes={en=skinned, boneless (about 800g)}}, Ingredient{q=250 ML, item=NamedItem{name=hot chicken stock, canonical=CanonicalItem{name=hot chicken stock}}}, Ingredient{q=2 TBSP, item=NamedItem{name=Chopped Coriander, canonical=CanonicalItem{name=Chopped Coriander, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={HERB=true}}}}]"));
	}

	@Test
	public void parseIngredientsChCashBlackSpiceCurry() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 KG, item=NamedItem{name=chicken, canonical=CanonicalItem{name=chicken}}, notes={en=skinned}}, Ingredient{q=6, item=NamedItem{name=Cloves, canonical=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=100 GRAMMES, item=NamedItem{name=coconut grated, canonical=CanonicalItem{name=coconut grated}}}, Ingredient{q=3 INCH, item=NamedItem{name=Cinnamon Stick, canonical=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=12, item=NamedItem{name=plump garlic cloves, canonical=CanonicalItem{name=plump garlic cloves}}, notes={en=peeled}}, Ingredient{q=225 GRAMMES, item=NamedItem{name=cashew nuts, canonical=CanonicalItem{name=cashew nuts}}}, Ingredient{q=1, item=NamedItem{name=large onion, canonical=CanonicalItem{name=large onion}}, notes={en=chopped}}, Ingredient{q=0 TBSP, item=NamedItem{name=Coriander Seeds, canonical=CanonicalItem{name=Coriander Seeds, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}}, Ingredient{q=0 TSP, item=NamedItem{name=Cumin Seeds, canonical=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}}, Ingredient{q=4 TBSP, item=NamedItem{name=oil, canonical=CanonicalItem{name=oil}}}, Ingredient{q=4, item=NamedItem{name=whole dried red chillies, canonical=CanonicalItem{name=whole dried red chillies}}}, Ingredient{q=1, item=NamedItem{name=salt, canonical=CanonicalItem{name=salt}}}]"));
	}

	@Test
	public void testSimilarity() throws IOException, IncompatibleIngredientsException {
		final List<IIngredient> ingr1 = parseIngredientsFrom("inputs.txt");
		final List<IIngredient> ingr2 = parseIngredientsFrom("inputs2.txt");
		final List<IIngredient> ingr3 = parseIngredientsFrom("inputs3.txt");
		final List<IIngredient> ingr4 = parseIngredientsFrom("chCashBlackSpiceCurry.txt");

		final double s12 = Similarity.amongIngredients( ingr1, ingr2);
		final double s13 = Similarity.amongIngredients( ingr1, ingr3);
		final double s23 = Similarity.amongIngredients( ingr2, ingr3);
		final double s34 = Similarity.amongIngredients( ingr3, ingr4);

		System.out.println(s12);
		System.out.println(s13);
		System.out.println(s23);
		System.out.println(s34);

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
		assertThat( jn.size(), is(2));
	}
}
