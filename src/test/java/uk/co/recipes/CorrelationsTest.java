/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ITag;
import uk.co.recipes.corr.Correlations;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.similarity.IncompatibleIngredientsException;

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

	private EsItemFactory itemFactory = GRAPH.get( EsItemFactory.class );
	private EsRecipeFactory recipeFactory = GRAPH.get( EsRecipeFactory.class );
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

        while ( recipeFactory.listAll().size() < 4) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        // For all the recipes (amongst those 4) that contain Olive Oil, Garlic, and Onions, what are the most popular of the shared ingredients?
        final String expected = "[CanonicalItem{name=Minced Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=x 400g cans chopped tomatoes}, CanonicalItem{name=Sugar, tags={SUGAR=true}}, CanonicalItem{name=Bay Leaf, tags={HERB=true}}, CanonicalItem{name=Red Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}, CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, CanonicalItem{name=Celery Sticks, parent=CanonicalItem{name=Celery, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}, CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Bacon Rashers, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Carrots, tags={VEGETABLE=true}}, CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}]";
		assertThat( corrs.findCountsWith( itemFactory.get("olive oil").get(), itemFactory.get("Garlic Cloves").get(), itemFactory.get("Onions").get()).toString(), is(expected));

		final Multiset<ITag> withTags = corrs.findTagsWith( itemFactory.get("Coriander").get(), itemFactory.get("Chicken Stock").get(), itemFactory.get("Star Anise").get() , itemFactory.get("Cumin Seeds").get());
		assertThat( withTags.toString(), is("[SPICE x 2, CHINESE, HERB, INDIAN, MEAT, SAUCE]"));

		final Multiset<ITag> withoutTags = corrs.findTagsWithout( itemFactory.get("Coriander").get() );
		assertThat( withoutTags.toString(), is("[VEGETABLE x 33, SPICE x 22, INDIAN x 19, MEAT x 19, CHINESE x 13, DAIRY x 12, FAT x 10, HERB x 9, ALCOHOL x 8, SAUCE x 8, SUGAR x 7, THAI x 7, CHILLI x 6, OIL x 6, WINE x 6, POULTRY x 5, FLOUR x 4, FRUIT x 4, ITALIAN x 4, SEED x 4, VINEGAR x 3, CHEESE x 2, FRENCH x 2, NUT x 2, PASTA x 2, SEAFOOD x 2, SPANISH x 2, VIETNAMESE x 2, EGG, HUNGARIAN, OFFAL, SALT]"));
	}

	@AfterClass
	public void shutDown() {
		itemFactory.stopES();
	}
}