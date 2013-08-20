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
import uk.co.recipes.similarity.IncompatibleIngredientsException;
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
	public void testCorrelations() throws IOException, IncompatibleIngredientsException, InterruptedException {
		dataUtils.parseIngredientsFrom("venisonBurgundy.txt");
		dataUtils.parseIngredientsFrom("bol1.txt");
		dataUtils.parseIngredientsFrom("bol2.txt");
		dataUtils.parseIngredientsFrom("chineseBeef.txt");

        while ( recipeFactory.countAll() < 4) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        // For all the recipes (amongst those 4) that contain Olive Oil, Garlic, and Onions, what are the most popular of the shared ingredients?
        final String expected = "[CanonicalItem{name=Bay Leaf, tags={HERB=true}} x 2, CanonicalItem{name=Carrot, tags={VEGETABLE=true}} x 2, CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}} x 2, CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}} x 2, CanonicalItem{name=Chinese five-spice powder, tags={SPICE=true, CHINESE=true}}, CanonicalItem{name=Sugar, tags={SUGAR=true}}, CanonicalItem{name=Braising Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Allspice, tags={SPICE=true}}, CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}, CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, CanonicalItem{name=Pearl Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Light Muscovado Sugar, parent=CanonicalItem{name=Brown Sugar, parent=CanonicalItem{name=Sugar, tags={SUGAR=true}}, tags={SUGAR=true}}, tags={SUGAR=true}}, CanonicalItem{name=Yellow Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Rosemary, tags={HERB=true}}, CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, CanonicalItem{name=Red Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}, CanonicalItem{name=Beef Stock, tags={MEAT=true, SAUCE=true}}, CanonicalItem{name=steamed bok choi and steamed basmati rice}, CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, CanonicalItem{name=Salt, tags={SALT=true}}, CanonicalItem{name=Soy Sauce, tags={SAUCE=true, CHINESE=true}}, CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}, CanonicalItem{name=Thyme, tags={HERB=true}}, CanonicalItem{name=Minced Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Bacon, tags={MEAT=true}}, CanonicalItem{name=all-purpose flour}, CanonicalItem{name=strong beef stock}, CanonicalItem{name=White Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true}}, tags={CHILLI=true, INDIAN=true, CHINESE=true, THAI=true}}, CanonicalItem{name=venison chuck}, CanonicalItem{name=Ginger, tags={SPICE=true, INDIAN=true, CHINESE=true}}, CanonicalItem{name=Parsley, tags={HERB=true}}, CanonicalItem{name=Star Anise, tags={SPICE=true, CHINESE=true}}, CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Bacon Rashers, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}, CanonicalItem{name=Chinese cooking wine or dry sherry}, CanonicalItem{name=Celery, tags={VEGETABLE=true}}]";
		assertThat( corrs.findCountsWith( itemFactory.get("olive oil").get(), itemFactory.get("Garlic Cloves").get(), itemFactory.get("Onion").get()).toString(), is(expected));

		final Multiset<ITag> withTags = corrs.findTagsWith( itemFactory.get("Coriander").get(), itemFactory.get("Chicken Stock").get(), itemFactory.get("Star Anise").get() , itemFactory.get("Cumin Seeds").get());
		assertThat( withTags.toString(), is("[SPICE x 2, CHINESE, HERB, INDIAN, MEAT, SAUCE]"));

		final Multiset<ITag> withoutTags = corrs.findTagsWithout( itemFactory.get("Coriander").get() );
		assertThat( withoutTags.toString(), is("[VEGETABLE x 42, SPICE x 29, INDIAN x 26, MEAT x 20, FRUIT x 18, DAIRY x 15, HERB x 15, CITRUS x 11, ITALIAN x 11, FAT x 10, SAUCE x 10, CHINESE x 9, FISH x 9, SEAFOOD x 9, SUGAR x 9, ALCOHOL x 8, NUT x 8, CHILLI x 7, NOODLES x 6, OIL x 6, SEED x 6, THAI x 6, WINE x 6, CHEESE x 5, CONDIMENT x 5, POULTRY x 5, RICE x 5, EGG x 4, FLOUR x 4, PASTA x 4, VIETNAMESE x 4, MEXICAN x 3, SPANISH x 3, VINEGAR x 3, FRENCH x 2, GRAIN x 2, JAPANESE x 2, PULSE x 2, WHEAT x 2, GREEK, HUNGARIAN, OFFAL, SALT]"));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}