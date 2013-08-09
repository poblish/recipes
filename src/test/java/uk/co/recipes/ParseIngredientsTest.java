package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.tags.CommonTags.*;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
import uk.co.recipes.similarity.Similarity;
import uk.co.recipes.test.TestDataUtils;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class ParseIngredientsTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();
		Thread.sleep(1000);
	}

	@Test
	public void parseIngredientsBulk() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("bulk.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=500 GRAMMES, item=CanonicalItem{name=Potato, tags={VEGETABLE=true}}, notes={en=[cut into chunks]}}, Ingredient{q=85 GRAMMES, item=CanonicalItem{name=Broccoli, tags={VEGETABLE=true}}, notes={en=[cut into small florets]}}, Ingredient{q=2, item=CanonicalItem{name=poached salmon fillets}, notes={en=[pack of]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true, CITRUS=true}}, notes={en=[Juice of]}}, Ingredient{q=SMALL BUNCHES, item=CanonicalItem{name=Dill, tags={HERB=true}}, notes={en=[chopped]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Dijon Mustard, parent=CanonicalItem{name=Mustard, tags={SAUCE=true}}, tags={SAUCE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Avocado, tags={FRUIT=true, MEXICAN=true}}, notes={en=[peeled, stoned and roughly chopped]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Cherry Tomatoes, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[halved]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=bag watercress}}, Ingredient{q=SMALL PIECE, item=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}}, Ingredient{q=2, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Lime, tags={FRUIT=true, CITRUS=true}}, notes={en=[Juice of]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Clear Honey, parent=CanonicalItem{name=Honey, tags={CONDIMENT=true}}, tags={CONDIMENT=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Soy Sauce, tags={SAUCE=true, CHINESE=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={SPICE=true, INDIAN=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Smooth Peanut Butter, parent=CanonicalItem{name=Peanut Butter, parent=CanonicalItem{name=Peanuts, tags={NUT=true}}, tags={NUT=true, CONDIMENT=true}}, tags={NUT=true, CONDIMENT=true}}}, Ingredient{q=500 GRAMMES, item=CanonicalItem{name=pack skinless chicken breast fillets}}, Ingredient{q=165 ML, item=CanonicalItem{name=Coconut Milk, parent=CanonicalItem{name=Coconut, tags={NUT=true, FRUIT=true, INDIAN=true, THAI=true, VIETNAMESE=true}}, tags={NUT=true, FRUIT=true, INDIAN=true, THAI=true, VIETNAMESE=true}}, notes={en=[can]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=Some, item=CanonicalItem{name=rice and lime wedges}, notes={en=[to serve, cooked]}}, Ingredient{q=1, item=CanonicalItem{name=Cucumber, tags={VEGETABLE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=White Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Golden Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=Some, item=CanonicalItem{name=sweet chilli sauce (optional)}}, Ingredient{q=1 BUNCHES, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[leaves picked (optional)]}}, Ingredient{q=7, item=CanonicalItem{name=Eggs, tags={EGG=true}}, notes={en=[separated, large]}}, Ingredient{q=150 GRAMMES, item=CanonicalItem{name=caster sugar or vanilla sugar}}, Ingredient{q=1 TSP, item=CanonicalItem{name=vanilla extract}}, Ingredient{q=150 GRAMMES, item=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}, notes={en=[sifted]}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Hazelnuts, tags={NUT=true}}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Chocolate}, notes={en=[dark]}}, Ingredient{q=6, item=CanonicalItem{name=Egg Yolks}, notes={en=[large]}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=225 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, tags={FAT=true, DAIRY=true}}, notes={en=[softened]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Chocolate}, notes={en=[dark]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, tags={FAT=true, DAIRY=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Double Cream, parent=CanonicalItem{name=Cream, tags={DAIRY=true}}, tags={DAIRY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Brown Rice Noodles, parent=CanonicalItem{name=Rice Noodles, parent=CanonicalItem{name=Noodles, tags={NOODLES=true}}, tags={EGG=true, RICE=true, NOODLES=true}}, tags={EGG=true, RICE=true, NOODLES=true}}}, Ingredient{q=500 ML, item=CanonicalItem{name=chicken or fish stock}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Thai red curry paste}}, Ingredient{q=4, item=CanonicalItem{name=or fresh kaffir lime leaves}, notes={en=[dried]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Fish Sauce, tags={FISH=true, SAUCE=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=white fish}, notes={en=[such as pollack, skinless, sustainable]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=King Prawns, parent=CanonicalItem{name=Prawns, tags={SEAFOOD=true}}, tags={SEAFOOD=true}}, notes={en=[raw]}}, Ingredient{q=2, item=CanonicalItem{name=Pak Choi, tags={VEGETABLE=true, CHINESE=true}}, notes={en=[leaves separated]}}, Ingredient{q=1 HANDFUL, item=CanonicalItem{name=Coriander, tags={HERB=true}}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, tags={FAT=true, DAIRY=true}}, notes={en=[at room temperature]}}, Ingredient{q=270 GRAMMES, item=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Baking Powder}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Golden Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=6, item=CanonicalItem{name=Eggs, tags={EGG=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Cocoa}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Instant Coffee, parent=CanonicalItem{name=Coffee}}, notes={en=[dissolved in 1 tbsp boiling water]}}, Ingredient{q=100 ML, item=CanonicalItem{name=espresso or strong instant coffee}, notes={en=[cooled]}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Tia Maria, tags={ALCOHOL=true}}}, Ingredient{q=250 ML, item=CanonicalItem{name=tub mascarpone}}, Ingredient{q=568 ML, item=CanonicalItem{name=pot double cream}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Amaretto, tags={ALCOHOL=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Chocolate}, notes={en=[dark]}}, Ingredient{q=1 KG, item=CanonicalItem{name=Potato, tags={VEGETABLE=true}}, notes={en=[peeled and quartered]}}, Ingredient{q=200 ML, item=CanonicalItem{name=Milk, tags={DAIRY=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=wholegrain mustard}}, Ingredient{q=2 BUNCHES, item=CanonicalItem{name=Spring Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[washed and sliced]}}, Ingredient{q=1 KG, item=CanonicalItem{name=Lamb Shoulder, parent=CanonicalItem{name=Lamb, tags={MEAT=true}}, tags={MEAT=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=3, item=CanonicalItem{name=Oregano, tags={HERB=true, ITALIAN=true}}, notes={en=[leaves stripped from 2]}}, Ingredient{q=3, item=CanonicalItem{name=Rosemary, tags={HERB=true}}, notes={en=[leaves stripped from 2]}}, Ingredient{q=3, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=600 ML, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}}, Ingredient{q=2, item=CanonicalItem{name=x 400g cans chopped tomatoes}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 KG, item=CanonicalItem{name=Pork Shoulder, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={MEAT=true}}, notes={en=[boned and rolled]}}, Ingredient{q=2, item=CanonicalItem{name=Onions, tags={VEGETABLE=true}}, notes={en=[sliced]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Paprika, tags={SPICE=true, SPANISH=true, HUNGARIAN=true}}, notes={en=[smoked]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Caraway Seeds, tags={SPICE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[finely chopped]}}, Ingredient{q=400 GRAMMES, item=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, notes={en=[can, chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Red Pepper, tags={VEGETABLE=true}}, notes={en=[deseeded and cut into wedges]}}, Ingredient{q=1 KG, item=CanonicalItem{name=Potato, tags={VEGETABLE=true}}, notes={en=[cut into quarters]}}, Ingredient{q=1, item=CanonicalItem{name=Cabbage, tags={VEGETABLE=true}}, notes={en=[finely sliced]}}, Ingredient{q=1, item=CanonicalItem{name=Soured Cream, parent=CanonicalItem{name=Cream, tags={DAIRY=true}}, tags={DAIRY=true}}, notes={en=[to serve]}}, Ingredient{q=2 KG, item=CanonicalItem{name=Pork Loin, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={MEAT=true}}}, Ingredient{q=7, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[peeled and cut into thin slivers]}}, Ingredient{q=7 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true, CITRUS=true}}, notes={en=[juice only]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}, notes={en=[dried]}}, Ingredient{q=8, item=CanonicalItem{name=sprigs fresh oregano}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Shallot, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Celeriac, tags={VEGETABLE=true}}, notes={en=[quartered and peeled, large]}}, Ingredient{q=1, item=CanonicalItem{name=x 130g twin-pack cubetti di pancetta}, notes={en=[(small cubes of Italian cured belly pork)]}}, Ingredient{q=600 ML, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}, notes={en=[; drink the remainder!, full-bodied]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Diced Chicken, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true}}, tags={MEAT=true, POULTRY=true}}, notes={en=[breast or dark meat as you prefer]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water or Chicken Stock}}, Ingredient{q=2, item=CanonicalItem{name=white onions finely chopped}, notes={en=[small]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={FAT=true, DAIRY=true, INDIAN=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={SPICE=true, INDIAN=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Powder, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=3 TSP, item=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Mustard Seeds, tags={SEED=true}}, notes={en=[whole]}}, Ingredient{q=3, item=CanonicalItem{name=Carrots boiled and pureed}}, Ingredient{q=100 ML, item=CanonicalItem{name=Single Cream, parent=CanonicalItem{name=Cream, tags={DAIRY=true}}, tags={DAIRY=true}}}, Ingredient{q=100 ML, item=CanonicalItem{name=Natural Yogurt, parent=CanonicalItem{name=Yogurt, tags={DAIRY=true}}, tags={DAIRY=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=200 ML, item=CanonicalItem{name=Pureed Onion}}, Ingredient{q=Some, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[to garnish, roughly, chopped, fresh]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Diced Chicken, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true}}, tags={MEAT=true, POULTRY=true}}, notes={en=[breast or dark meat as you prefer]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water or Chicken Stock}}, Ingredient{q=2, item=CanonicalItem{name=white onions finely chopped}, notes={en=[small]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={FAT=true, DAIRY=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={SPICE=true, INDIAN=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=7 TSP, item=CanonicalItem{name=Chilli Powder, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Sizzling Seeds, tags={SEED=true, INDIAN=true}}, notes={en=[whole]}}, Ingredient{q=1, item=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, notes={en=[tin, chopped]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=300 ML, item=CanonicalItem{name=Pureed Onion}}, Ingredient{q=8, item=CanonicalItem{name=Bird's eye chili, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, THAI=true, VIETNAMESE=true}}, notes={en=[sliced lengthways to garnish]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Prawns, tags={SEAFOOD=true}}, notes={en=[cooked or uncooked, small, peeled]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water or Prawn Stock}}, Ingredient{q=2, item=CanonicalItem{name=white onions finely chopped}, notes={en=[small]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={FAT=true, DAIRY=true, INDIAN=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={SPICE=true, INDIAN=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Powder, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seeds, tags={SPICE=true, SEED=true}}, notes={en=[whole]}}, Ingredient{q=200 ML, item=CanonicalItem{name=Pineapple Juice and 100ml Mango Chutney}}, Ingredient{q=50 ML, item=CanonicalItem{name=Natural Yogurt, parent=CanonicalItem{name=Yogurt, tags={DAIRY=true}}, tags={DAIRY=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=200 ML, item=CanonicalItem{name=Pureed Onion}}, Ingredient{q=400 ML, item=CanonicalItem{name=Pureed Lentils}}, Ingredient{q=Some, item=CanonicalItem{name=coriander leaves to garnish}, notes={en=[roughly, chopped, fresh]}}]"));
	}

	@Test
	public void parseIngredients1() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("inputs.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Streaky Bacon, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, notes={en=[preferably in one piece, skinned and cut into pieces, smoked]}}, Ingredient{q=900 GRAMMES, item=CanonicalItem{name=lamb neck fillets}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Baby Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=5, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Button Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[small]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=3, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=SMALL BUNCHES, item=CanonicalItem{name=Thyme, tags={HERB=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=lamb or beef stock}}, Ingredient{q=LARGE SPLASHES, item=CanonicalItem{name=Worcestershire Sauce, tags={FISH=true, SAUCE=true}}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, notes={en=[including thyme, rosemary and parsley, chopped]}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, notes={en=[grated, chilled]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true, CITRUS=true}}, notes={en=[Juice of]}}, Ingredient{q=5, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=1, item=CanonicalItem{name=Eggs, tags={EGG=true}}, notes={en=[to glaze, beaten]}}]"));
	}

	@Test
	public void parseIngredients2() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("inputs2.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Gnocchi, tags={PASTA=true, ITALIAN=true}}, notes={en=[fresh]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[sliced, deseeded if you like]}}, Ingredient{q=1, item=CanonicalItem{name=Courgette, tags={VEGETABLE=true}}, notes={en=[cut into thin ribbons with a peeler, medium]}}, Ingredient{q=4, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={FRUIT=true, CITRUS=true}}, notes={en=[Juice of]}}, Ingredient{q=2 HEAPED_TBSP, item=CanonicalItem{name=Mascarpone, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}, notes={en=[(or vegetarian alternative), grated]}}, Ingredient{q=Some, item=CanonicalItem{name=Mixed Leaves, tags={VEGETABLE=true}}, notes={en=[to serve, dressed]}}]"));
	}

	@Test
	public void parseIngredients3() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("inputs3.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[large]}}, Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}}, Ingredient{q=5 CM, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Flakes, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Garam Masala, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}}, Ingredient{q=400 GRAMMES, item=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, notes={en=[can, chopped]}}, Ingredient{q=8, item=CanonicalItem{name=Chicken Thighs, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true}}, tags={MEAT=true, POULTRY=true}}, notes={en=[skinned, boneless (about 800g)]}}, Ingredient{q=250 ML, item=CanonicalItem{name=Chicken Stock, tags={MEAT=true, SAUCE=true}}, notes={en=[hot]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[chopped]}}]"));
	}

	@Test
	public void parseIngredientsChCashBlackSpiceCurry() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 KG, item=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true}}, notes={en=[skinned]}}, Ingredient{q=6, item=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Coconut, tags={NUT=true, FRUIT=true, INDIAN=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=3 INCH, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true}}, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=12, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[peeled, plump]}}, Ingredient{q=225 GRAMMES, item=CanonicalItem{name=Cashew Nuts, tags={NUT=true}}}, Ingredient{q=0 INCH, item=CanonicalItem{name=piece of fresh ginger}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[chopped, large]}}, Ingredient{q=0 TBSP, item=CanonicalItem{name=Coriander Seeds, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, notes={en=[coarse]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}}, Ingredient{q=4, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[whole, dried]}}, Ingredient{q=1, item=CanonicalItem{name=Salt, tags={SALT=true}}}]"));
	}

	@Test
	public void parseIngredientsVenisonBurgundy() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("venisonBurgundy.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=3 CUP, item=CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}, notes={en=[dry]}}, Ingredient{q=2 CUP, item=CanonicalItem{name=strong beef stock}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, notes={en=[(or good quality brandy)]}}, Ingredient{q=1, item=CanonicalItem{name=Yellow Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[chopped into large pieces, large]}}, Ingredient{q=2, item=CanonicalItem{name=Carrots, tags={VEGETABLE=true}}, notes={en=[peeled and cut into 2-inch-long pieces]}}, Ingredient{q=3 CLOVE, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[crushed and chopped]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Parsley, tags={HERB=true}}, notes={en=[chopped, fresh]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Thyme, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Rosemary, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=10, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=3, item=CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}, notes={en=[whole]}}, Ingredient{q=1, item=CanonicalItem{name=Allspice, tags={SPICE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=3 POUNDS, item=CanonicalItem{name=venison chuck}, notes={en=[(shoulder cuts), cut into 2-inch chunks]}}, Ingredient{q=0 POUNDS, item=CanonicalItem{name=Bacon, tags={MEAT=true}}, notes={en=[cut into thick slices, and then coarsely chopped]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=plus 1 tablespoon softened butter}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Pearl Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=White Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[wiped clean and bottoms trimmed]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Salt, tags={SALT=true}}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=all-purpose flour}}]"));
	}

	@Test
	public void parseIngredientsBeefStewOrzo() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("beefStewOrzo.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=0 POUNDS, item=CanonicalItem{name=beef or lamb}, notes={en=[cut into 2-inch chunks (see note)]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, notes={en=[(separated)]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[diced, large]}}, Ingredient{q=4 CLOVE, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[minced finely]}}, Ingredient{q=1, item=CanonicalItem{name=Leek, tags={VEGETABLE=true}}, notes={en=[(cleaned, trimmed and cut in half), large]}}, Ingredient{q=1, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}, notes={en=[cut into thirds, large]}}, Ingredient{q=0 CUP, item=CanonicalItem{name=White Wine, tags={WINE=true, ALCOHOL=true}}, notes={en=[dry]}}, Ingredient{q=3, item=CanonicalItem{name=- 4 whole allspice berries}}, Ingredient{q=28 OUNCES, item=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, notes={en=[can, crushed]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Sugar, tags={SUGAR=true}}}, Ingredient{q=1 QUART, item=CanonicalItem{name=Water}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Orzo, tags={PASTA=true, ITALIAN=true}}}, Ingredient{q=1, item=CanonicalItem{name=salt and pepper to taste}}]"));
	}

	@Test
	public void parseIngredientsTtFishCurry() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("ttFishCurry.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[roughly chopped (deseeded if you don't like it too hot)]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[peeled and roughly chopped]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Coriander Powder, parent=CanonicalItem{name=Coriander, tags={HERB=true}}, tags={SPICE=true, HERB=true, INDIAN=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Rapeseed Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, tags={SPICE=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seed, tags={SPICE=true, SEED=true}}}, Ingredient{q=2, item=CanonicalItem{name=x 400g cans chopped tomatoes}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Green Beans, tags={VEGETABLE=true}}, notes={en=[trimmed and halved]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tamarind Paste, tags={FRUIT=true, SAUCE=true}}}, Ingredient{q=4, item=CanonicalItem{name=white fish fillets}, notes={en=[(we used hake), firm]}}, Ingredient{q=1 HANDFUL, item=CanonicalItem{name=Coriander, tags={HERB=true}}, notes={en=[roughly chopped]}}, Ingredient{q=Some, item=CanonicalItem{name=Basmati Rice, parent=CanonicalItem{name=Rice, tags={RICE=true}}, tags={RICE=true, INDIAN=true}}, notes={en=[to serve, cooked]}}]"));
	}

	@Test
	public void parseIngredientsNoodles() throws IOException {
		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom("noodles.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[roughly chopped, large]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[fresh]}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, notes={en=[finely chopped (seeds in or out, you decide), long]}}, Ingredient{q=1, item=CanonicalItem{name=Garlic Cloves, tags={VEGETABLE=true}}, notes={en=[crushed]}}, Ingredient{q=6, item=CanonicalItem{name=White Peppercorns, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, notes={en=[crushed]}}, Ingredient{q=20 GRAMMES, item=CanonicalItem{name=pack coriander}, notes={en=[stalks, roots if you have them and leaves, chopped and kept separate, plus sprigs to finish]}}, Ingredient{q=50 ML, item=CanonicalItem{name=Milk, tags={DAIRY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=white breadcrumbs}, notes={en=[fresh]}}, Ingredient{q=1 KG, item=CanonicalItem{name=Chicken Mince, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true}}, tags={MEAT=true, POULTRY=true}}, notes={en=[quality]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}}, Ingredient{q=1 LITRE, item=CanonicalItem{name=Chicken Stock, tags={MEAT=true, SAUCE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Sesame Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true}}, tags={OIL=true, FAT=true, CHINESE=true}}, notes={en=[toasted]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Fish Sauce, tags={FISH=true, SAUCE=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=6, item=CanonicalItem{name=Star Anise, tags={SPICE=true, CHINESE=true}}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, tags={SPICE=true, INDIAN=true, CHINESE=true}}, notes={en=[sliced, fresh]}}, Ingredient{q=0 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=8, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[thinly sliced]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Egg Noodles, parent=CanonicalItem{name=Noodles, tags={NOODLES=true}}, tags={EGG=true, NOODLES=true}}, notes={en=[cooked]}}, Ingredient{q=Some, item=CanonicalItem{name=sliced chillies to taste (optional)}}, Ingredient{q=1, item=CanonicalItem{name=bunch basil}, notes={en=[leaves picked, small]}}]"));
	}

	@Test
	public void testAliases() throws IOException, IncompatibleIngredientsException {
		final List<IIngredient> namings1 = dataUtils.parseIngredientsFrom("namings1.txt");
		final List<IIngredient> namings2 = dataUtils.parseIngredientsFrom("namings2.txt");
		assertThat( Similarity.amongIngredients( namings1, namings2), is(1.0));
		assertThat( namings1, is(namings2));
		assertThat( namings2, is(namings1));
	}

	@Test
	public void testSimilarity() throws IOException, IncompatibleIngredientsException {
    	final List<IIngredient> ingr1 = dataUtils.parseIngredientsFrom("inputs.txt");
		final List<IIngredient> ingr2 = dataUtils.parseIngredientsFrom("inputs2.txt");
		final List<IIngredient> ingr3 = dataUtils.parseIngredientsFrom("inputs3.txt");
		final List<IIngredient> ingr4 = dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		final List<IIngredient> ingrBol1 = dataUtils.parseIngredientsFrom("bol1.txt");
		final List<IIngredient> ingrBol2 = dataUtils.parseIngredientsFrom("bol2.txt");
		final List<IIngredient> ingrChBeef = dataUtils.parseIngredientsFrom("chineseBeef.txt");

		assertThat( Categorisation.forIngredients(ingr1).toString(), is("[ALCOHOL, CITRUS, DAIRY, EGG, FAT x 2, FISH, FLOUR x 2, FRUIT, HERB x 4, MEAT, OIL, SAUCE, VEGETABLE x 3, WINE]"));
		assertThat( Categorisation.forIngredients(ingr2).toString(), is("[CHEESE x 2, CHILLI, CHINESE, CITRUS, DAIRY x 2, FAT, FRUIT, INDIAN, ITALIAN x 3, OIL, PASTA, THAI, VEGETABLE x 3]"));
		assertThat( Categorisation.forIngredients(ingr3).toString(), is("[CHILLI, CHINESE, FAT, HERB, INDIAN x 5, MEAT x 2, OIL, POULTRY, SAUCE, SEED, SPICE x 6, SUGAR, VEGETABLE x 3]"));
		assertThat( Categorisation.forIngredients(ingr4).toString(), is("[CHILLI, CHINESE, FAT, FRUIT, HERB, INDIAN x 6, MEAT, NUT x 2, OIL, POULTRY, SALT, SPICE x 5, THAI x 2, VEGETABLE x 2, VIETNAMESE]"));
		assertThat( Categorisation.forIngredients(ingrBol1).toString(), is("[ALCOHOL, CHEESE, DAIRY, FAT, HERB x 2, ITALIAN, MEAT x 2, OIL, SUGAR, VEGETABLE x 6, VINEGAR, WINE]"));
		assertThat( Categorisation.forIngredients(ingrBol2).toString(), is("[ALCOHOL, DAIRY x 2, FAT, MEAT x 3, OFFAL, POULTRY, SPICE, VEGETABLE x 4, WINE]"));
		assertThat( Categorisation.forIngredients(ingrChBeef).toString(), is("[CHILLI, CHINESE x 5, FAT, FLOUR, INDIAN x 2, MEAT x 2, OIL, SAUCE x 2, SPICE x 3, SUGAR, THAI, VEGETABLE x 2]"));

		final ITag[] tags = new ITag[]{ INDIAN, CHINESE, JAPANESE, THAI, FRENCH, ITALIAN, GREEK, ENGLISH };
		assertThat( Categorisation.forIngredients(ingr1, tags).toString(), is("[]"));
		assertThat( Categorisation.forIngredients(ingr2, tags).toString(), is("[CHINESE, INDIAN, ITALIAN x 3, THAI]"));
		assertThat( Categorisation.forIngredients(ingr3, tags).toString(), is("[CHINESE, INDIAN x 5]"));
		assertThat( Categorisation.forIngredients(ingr4, tags).toString(), is("[CHINESE, INDIAN x 6, THAI x 2]"));
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

    @Test
    public void testParseFailures() {
    	int numSuccesses = 0;
    	numSuccesses += dataUtils.parseIngredient("100g beef or lamb").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("- 4 whole allspice berries").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g orzo pasta").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("salt and pepper to taste").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("x 400g cans chopped tomatoes").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("poached salmon fillets").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("bag watercress").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tsp clear honey").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp mild curry powder").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g smooth peanut butter").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("pack skinless chicken breast fillets").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100ml coconut milk").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("rice and lime wedges").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("sweet chilli sauce (optional)").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g caster sugar or vanilla sugar").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tsp vanilla extract").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("brown rice noodles").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("chicken or fish stock").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("Thai red curry paste").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("or fresh kaffir lime leaves").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g white fish").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g espresso or strong instant coffee").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("tub mascarpone").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("pot double cream").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp wholegrain mustard").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("shoulder of lamb").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("2.5 kg British pork loin").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("sprigs fresh oregano").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("x 130g twin-pack cubetti di pancetta").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100ml Water or Chicken Stock").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g white onions finely chopped").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp Ginger Puree").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp Garlic Puree").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g Carrots boiled and pureed").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp Pureed Onion").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("Water or Prawn Stock").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("Pineapple Juice and 100ml Mango Chutney").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("1 tbsp Pureed Lentils").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("coriander leaves to garnish").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g coconut grated").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("piece of fresh ginger").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("Chinese cooking wine or dry sherry").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("steamed bok choi and steamed basmati rice").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("lamb neck fillets").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("lamb or beef stock").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("pack coriander").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g white breadcrumbs").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g egg noodles").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("sliced chillies to taste (optional)").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("bunch basil").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("white fish fillets").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g strong beef stock").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("venison chuck").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("plus 1 tablespoon softened butter").isPresent() ? 1 : 0;
    	numSuccesses += dataUtils.parseIngredient("100g all-purpose flour").isPresent() ? 1 : 0;
    	// assertThat( numSuccesses, is(55));
    }

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}