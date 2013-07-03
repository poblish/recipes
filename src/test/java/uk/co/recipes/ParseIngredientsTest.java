package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;
import static uk.co.recipes.tags.CommonTags.*;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
import uk.co.recipes.similarity.Similarity;

public class ParseIngredientsTest {

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		CanonicalItemFactory.startES();
		CanonicalItemFactory.deleteAll();
		RecipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		ItemsLoader.load();
		Thread.sleep(1000);
	}

	@Test
	public void parseIngredients1() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Streaky Bacon, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, notes={en=[preferably in one piece, skinned and cut into pieces, smoked]}}, Ingredient{q=900 GRAMMES, item=CanonicalItem{name=lamb neck fillets}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Baby Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=5, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Button Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[small]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=3, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=SMALL BUNCHES, item=CanonicalItem{name=Thyme, tags={HERB=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=lamb or beef stock}}, Ingredient{q=LARGE SPLASHES, item=CanonicalItem{name=Worcestershire Sauce, tags={SAUCE=true}}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, notes={en=[including thyme, rosemary and parsley, chopped]}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, notes={en=[grated, chilled]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true}}, notes={en=[Juice of]}}, Ingredient{q=5, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=1, item=CanonicalItem{name=Egg, tags={EGG=true}}, notes={en=[to glaze, beaten]}}]"));
	}

	@Test
	public void parseIngredients2() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs2.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Gnocchi, tags={PASTA=true, ITALIAN=true}}, notes={en=[fresh]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chilli, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[sliced, deseeded if you like]}}, Ingredient{q=1, item=CanonicalItem{name=medium courgette}, notes={en=[cut into thin ribbons with a peeler]}}, Ingredient{q=4, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true}}, notes={en=[Juice of]}}, Ingredient{q=2 HEAPED_TBSP, item=CanonicalItem{name=Mascarpone, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}, notes={en=[(or vegetarian alternative), grated]}}, Ingredient{q=Some, item=CanonicalItem{name=Mixed Leaves, tags={VEGETABLE=true}}, notes={en=[to serve, dressed]}}]"));
	}

	@Test
	public void parseIngredients3() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("inputs3.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[large]}}, Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}}, Ingredient{q=5 CM, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Flakes, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=400 GRAMMES, item=CanonicalItem{name=Tomatoes, tags={VEGETABLE=true}}, notes={en=[can, chopped]}}, Ingredient{q=8, item=CanonicalItem{name=Chicken Thighs, parent=CanonicalItem{name=Chicken, tags={MEAT=true}}, tags={MEAT=true}}, notes={en=[skinned, boneless (about 800g)]}}, Ingredient{q=250 ML, item=CanonicalItem{name=Chicken Stock, tags={MEAT=true, SAUCE=true}}, notes={en=[hot]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[chopped]}}]"));
	}

	@Test
	public void parseIngredientsChCashBlackSpiceCurry() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 KG, item=CanonicalItem{name=Chicken, tags={MEAT=true}}, notes={en=[skinned]}}, Ingredient{q=6, item=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=coconut grated}}, Ingredient{q=3 INCH, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=12, item=CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled, plump]}}, Ingredient{q=225 GRAMMES, item=CanonicalItem{name=Cashew Nuts, tags={NUT=true}}}, Ingredient{q=0 INCH, item=CanonicalItem{name=piece of fresh ginger}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[chopped, large]}}, Ingredient{q=0 TBSP, item=CanonicalItem{name=Coriander Seeds, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, notes={en=[coarse]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}}, Ingredient{q=4, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[whole, dried]}}, Ingredient{q=1, item=CanonicalItem{name=Salt, tags={SALT=true}}}]"));
	}

	@Test
	public void parseIngredientsVenisonBurgundy() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("venisonBurgundy.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=3 CUP, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}, notes={en=[dry]}}, Ingredient{q=2 CUP, item=CanonicalItem{name=strong beef stock}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, notes={en=[(or good quality brandy)]}}, Ingredient{q=1, item=CanonicalItem{name=Yellow Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[chopped into large pieces, large]}}, Ingredient{q=2, item=CanonicalItem{name=Carrots, tags={VEGETABLE=true}}, notes={en=[peeled and cut into 2-inch-long pieces]}}, Ingredient{q=3 CLOVE, item=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, notes={en=[crushed and chopped]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Parsley, tags={HERB=true}}, notes={en=[chopped, fresh]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Thyme, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Rosemary, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=10, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=3, item=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}, notes={en=[whole]}}, Ingredient{q=1, item=CanonicalItem{name=Allspice, tags={SPICE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=3 POUNDS, item=CanonicalItem{name=venison chuck}, notes={en=[(shoulder cuts), cut into 2-inch chunks]}}, Ingredient{q=0 POUNDS, item=CanonicalItem{name=Bacon, tags={MEAT=true}}, notes={en=[cut into thick slices, and then coarsely chopped]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=plus 1 tablespoon softened butter}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Pearl Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=White Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[wiped clean and bottoms trimmed]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Salt, tags={SALT=true}}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=all-purpose flour}}]"));
	}

	@Test
	public void parseIngredientsBeefStewOrzo() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("beefStewOrzo.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=0 POUNDS, item=CanonicalItem{name=beef or lamb}, notes={en=[cut into 2-inch chunks (see note)]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, notes={en=[(separated)]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[diced, large]}}, Ingredient{q=4 CLOVE, item=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, notes={en=[minced finely]}}, Ingredient{q=1, item=CanonicalItem{name=Leek, tags={VEGETABLE=true}}, notes={en=[(cleaned, trimmed and cut in half), large]}}, Ingredient{q=1, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}, notes={en=[cut into thirds, large]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=White Wine, tags={WINE=true, ALCOHOL=true}}, notes={en=[dry]}}, Ingredient{q=3, item=CanonicalItem{name=- 4 whole allspice berries}}, Ingredient{q=28 OUNCES, item=CanonicalItem{name=Tomatoes, tags={VEGETABLE=true}}, notes={en=[can, crushed]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Sugar, tags={SUGAR=true}}}, Ingredient{q=1 QUART, item=CanonicalItem{name=Water}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Orzo, tags={PASTA=true, ITALIAN=true}}}, Ingredient{q=1, item=CanonicalItem{name=salt and pepper to taste}}]"));
	}

	@Test
	public void parseIngredientsTtFishCurry() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("ttFishCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chilli, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[roughly chopped (deseeded if you don't like it too hot)]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[peeled and roughly chopped]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Coriander Powder, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Rapeseed Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}}, Ingredient{q=2, item=CanonicalItem{name=x 400g cans chopped tomatoes}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=green beans}, notes={en=[trimmed and halved]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tamarind Paste, tags={FRUIT=true, SAUCE=true}}}, Ingredient{q=4, item=CanonicalItem{name=firm white fish fillets}, notes={en=[(we used hake)]}}, Ingredient{q=1 HANDFUL, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[roughly chopped]}}, Ingredient{q=Some, item=CanonicalItem{name=cooked basmati rice}, notes={en=[to serve]}}]"));
	}

	@Test
	public void parseIngredientsNoodles() throws IOException {
		final List<IIngredient> allIngredients = parseIngredientsFrom("noodles.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[roughly chopped, large]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[fresh]}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[finely chopped (seeds in or out, you decide), long]}}, Ingredient{q=1, item=CanonicalItem{name=garlic clove}, notes={en=[crushed]}}, Ingredient{q=6, item=CanonicalItem{name=white peppercorns}, notes={en=[crushed]}}, Ingredient{q=20 GRAMMES, item=CanonicalItem{name=pack coriander}, notes={en=[stalks, roots if you have them and leaves, chopped and kept separate, plus sprigs to finish]}}, Ingredient{q=50 ML, item=CanonicalItem{name=Milk, tags={DAIRY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=white breadcrumbs}, notes={en=[fresh]}}, Ingredient{q=1 KG, item=CanonicalItem{name=chicken mince}, notes={en=[quality]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 LITRE, item=CanonicalItem{name=Chicken Stock, tags={MEAT=true, SAUCE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Sesame Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true, CHINESE=true}}, notes={en=[toasted]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Fish Sauce, tags={MEAT=true, SAUCE=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=6, item=CanonicalItem{name=Star Anise, tags={SPICE=true, CHINESE=true}}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[sliced, fresh]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=8, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[thinly sliced]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=egg noodles}, notes={en=[cooked]}}, Ingredient{q=Some, item=CanonicalItem{name=sliced chillies to taste (optional)}}, Ingredient{q=1, item=CanonicalItem{name=bunch basil}, notes={en=[leaves picked, small]}}]"));
	}

	@Test
	public void testAliases() throws IOException, IncompatibleIngredientsException {
		final List<IIngredient> namings1 = parseIngredientsFrom("namings1.txt");
		final List<IIngredient> namings2 = parseIngredientsFrom("namings2.txt");
		assertThat( Similarity.amongIngredients( namings1, namings2), is(1.0));
		assertThat( namings1, is(namings2));
		assertThat( namings2, is(namings1));
	}

	@Test
	public void testSimilarity() throws IOException, IncompatibleIngredientsException {
		final List<IIngredient> ingr1 = parseIngredientsFrom("inputs.txt");
		final List<IIngredient> ingr2 = parseIngredientsFrom("inputs2.txt");
		final List<IIngredient> ingr3 = parseIngredientsFrom("inputs3.txt");
		final List<IIngredient> ingr4 = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		final List<IIngredient> ingrBol1 = parseIngredientsFrom("bol1.txt");
		final List<IIngredient> ingrBol2 = parseIngredientsFrom("bol2.txt");
		final List<IIngredient> ingrChBeef = parseIngredientsFrom("chineseBeef.txt");

		assertThat( Categorisation.forIngredients(ingr1).toString(), is("[ALCOHOL, DAIRY, EGG, FAT x 2, FLOUR x 2, FRUIT, HERB x 4, MEAT, OIL, SAUCE, VEGETABLE x 3, WINE]"));
		assertThat( Categorisation.forIngredients(ingr2).toString(), is("[CHEESE x 2, CHILLI, CHINESE, DAIRY x 2, FAT, FRUIT, INDIAN, ITALIAN x 3, OIL, PASTA, THAI, VEGETABLE x 2]"));
		assertThat( Categorisation.forIngredients(ingr3).toString(), is("[CHILLI, CHINESE x 2, FAT, HERB, INDIAN x 6, MEAT x 2, OIL, SAUCE, SEED, SPICE x 6, SUGAR, THAI, VEGETABLE x 3]"));
		assertThat( Categorisation.forIngredients(ingr4).toString(), is("[CHILLI, CHINESE, FAT, HERB, INDIAN x 5, MEAT, NUT, OIL, SALT, SPICE x 5, THAI, VEGETABLE x 2]"));
		assertThat( Categorisation.forIngredients(ingrBol1).toString(), is("[ALCOHOL, CHEESE, DAIRY, FAT, HERB x 2, ITALIAN, MEAT x 2, OIL, SUGAR, VEGETABLE x 5, VINEGAR, WINE]"));
		assertThat( Categorisation.forIngredients(ingrBol2).toString(), is("[ALCOHOL, DAIRY x 2, FAT, MEAT x 3, SPICE, VEGETABLE x 4, WINE]"));
		assertThat( Categorisation.forIngredients(ingrChBeef).toString(), is("[CHILLI, CHINESE x 5, FAT, FLOUR, INDIAN x 2, MEAT x 2, OIL, SAUCE x 2, SPICE x 3, SUGAR, THAI, VEGETABLE]"));

		final ITag[] tags = new ITag[]{ INDIAN, CHINESE, JAPANESE, THAI, FRENCH, ITALIAN, GREEK, ENGLISH };
		assertThat( Categorisation.forIngredients(ingr1, tags).toString(), is("[]"));
		assertThat( Categorisation.forIngredients(ingr2, tags).toString(), is("[CHINESE, INDIAN, ITALIAN x 3, THAI]"));
		assertThat( Categorisation.forIngredients(ingr3, tags).toString(), is("[CHINESE x 2, INDIAN x 6, THAI]"));
		assertThat( Categorisation.forIngredients(ingr4, tags).toString(), is("[CHINESE, INDIAN x 5, THAI]"));
		assertThat( Categorisation.forIngredients(ingrBol1, tags).toString(), is("[ITALIAN]"));
		assertThat( Categorisation.forIngredients(ingrBol2, tags).toString(), is("[]"));
		assertThat( Categorisation.forIngredients(ingrChBeef, tags).toString(), is("[CHINESE x 5, INDIAN x 2, THAI]"));

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
		// assertThat( Similarity.amongIngredients( ingr3, ingr2), is(s23));
	}

	@AfterClass
	public void shutDown() {
		CanonicalItemFactory.stopES();
	}
}
