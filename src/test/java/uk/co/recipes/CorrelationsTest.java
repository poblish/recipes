/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ITag;
import uk.co.recipes.corr.Correlations;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.test.TestDataUtils;

import com.google.common.collect.Multiset;

import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CorrelationsTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );
	private Correlations corrs = GRAPH.get( Correlations.class );

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
	public void testCorrelations() throws IOException, InterruptedException {
		dataUtils.parseIngredientsFrom("venisonBurgundy.txt");
		dataUtils.parseIngredientsFrom("bol1.txt");
		dataUtils.parseIngredientsFrom("bol2.txt");
		dataUtils.parseIngredientsFrom("chineseBeef.txt");

        while ( recipeFactory.countAll() < 4) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        // For all the recipes (amongst those 4) that contain Olive Oil, Garlic, and Onions, what are the most popular of the shared ingredients?
        final String expected = "[CanonicalItem{name=Bay Leaf, tags={HERB=true}} x 2, CanonicalItem{name=Carrot, tags={VEGETABLE=true}} x 2, CanonicalItem{name=Red Wine, tags={ALCOHOL=true, WINE=true}} x 2, CanonicalItem{name=Chinese five-spice powder, tags={CHINESE=true, SPICE=true}}, CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, CanonicalItem{name=Braising Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Allspice, tags={SPICE=true}}, CanonicalItem{name=Cloves, tags={INDIAN=true, SPICE=true}}, CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={ALCOHOL=true, FRENCH=true, SPIRIT=true}}, tags={ALCOHOL=true, FRENCH=true, SPIRIT=true}}, CanonicalItem{name=Pearl Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Light Muscovado Sugar, parent=CanonicalItem{name=Brown Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}, CanonicalItem{name=Yellow Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Rosemary, tags={HERB=true}}, CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, CanonicalItem{name=Red Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}, CanonicalItem{name=Beef Stock, tags={MEAT=true, STOCK=true}}, CanonicalItem{name=steamed bok choi and steamed basmati rice}, CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, CanonicalItem{name=Salt, tags={SALT=true}}, CanonicalItem{name=Soy Sauce, tags={CHINESE=true, SAUCE=true}}, CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={BAKING=true, FLOUR=true}}, tags={BAKING=true, FLOUR=true}}, CanonicalItem{name=Thyme, tags={HERB=true}}, CanonicalItem{name=Minced Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Bacon, tags={MEAT=true}}, CanonicalItem{name=all-purpose flour, tags={FLOUR=true}}, CanonicalItem{name=strong beef stock}, CanonicalItem{name=White Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, CHINESE=true, INDIAN=true, THAI=true}}, CanonicalItem{name=venison chuck}, CanonicalItem{name=Ginger, tags={CHINESE=true, INDIAN=true, SPICE=true}}, CanonicalItem{name=Parsley, tags={HERB=true}}, CanonicalItem{name=Star Anise, tags={CHINESE=true, SPICE=true}}, CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Tomato Purée, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={SAUCE=true, VEGETABLE=true}}, CanonicalItem{name=Bacon Rashers, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={SAUCE=true, VEGETABLE=true}}, CanonicalItem{name=Parmesan, tags={CHEESE=true, DAIRY=true, ITALIAN=true}}, CanonicalItem{name=Chinese cooking wine or dry sherry}, CanonicalItem{name=Celery, tags={VEGETABLE=true}}]";
        assertThat( corrs.findCountsWith( itemFactory.get("olive oil").get(), itemFactory.get("Garlic Cloves").get(), itemFactory.get("Onion").get()).toString(), is(expected));

		final Multiset<ITag> withTags = corrs.findTagsWith( itemFactory.get("Coriander").get(), itemFactory.get("Chicken Stock").get(), itemFactory.get("Star Anise").get() , itemFactory.get("Cumin Seeds").get());
		assertThat( withTags.toString(), is("[SPICE x 2, CHINESE, HERB, INDIAN, MEAT, STOCK]"));

		final Multiset<ITag> withoutTags = corrs.findTagsWithout( itemFactory.get("Coriander").get() );
		assertThat( withoutTags.toString(), is("[VEGETABLE x 59, INDIAN x 34, MEAT x 34, SPICE x 33, DAIRY x 30, BAKING x 29, FRUIT x 29, ITALIAN x 23, HERB x 22, SAUCE x 20, FAT x 17, SEAFOOD x 17, NUT x 16, FISH x 14, ALCOHOL x 13, CHEESE x 13, OIL x 13, CITRUS x 11, CHINESE x 10, SEED x 10, SPIRIT x 10, PULSE x 9, SUGAR x 9, CONDIMENT x 8, FRENCH x 8, POULTRY x 8, CHILLI x 7, EGG x 7, ORANGE x 7, THAI x 7, FLOUR x 6, NOODLES x 6, SPANISH x 6, VINEGAR x 6, LEMON x 5, PASTA x 5, RICE x 5, VIETNAMESE x 5, ALMOND x 4, MEXICAN x 4, ANISEED x 3, COFFEE x 3, GRAIN x 3, GREEK x 3, JAPANESE x 3, SALAD_LEAF x 3, STOCK x 3, WINE x 3, BREAD x 2, ENGLISH x 2, FLAVOURING x 2, HUNGARIAN x 2, SALT x 2, SAUSAGE x 2, SWEET x 2, WHEAT x 2, BLUE_CHEESE, DUTCH, GERMAN, OFFAL, SWISS]"));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}