/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ITag;
import uk.co.recipes.corr.Correlations;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.similarity.IncompatibleIngredientsException;

import com.google.common.collect.Multiset;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CorrelationsTest {

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
	public void testCorrelations() throws IOException, IncompatibleIngredientsException, InterruptedException {
		parseIngredientsFrom("venisonBurgundy.txt");
        parseIngredientsFrom("bol1.txt");
        parseIngredientsFrom("bol2.txt");
        parseIngredientsFrom("chineseBeef.txt");

        while ( RecipeFactory.listAll().size() < 4) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        // For all the recipes (amongst those 4) that contain Olive Oil, Garlic, and Onions, what are the most popular of the shared ingredients?
        final String expected = "[CanonicalItem{name=Minced Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=x 400g cans chopped tomatoes}, CanonicalItem{name=Sugar, tags={SUGAR=true}}, CanonicalItem{name=Bay Leaf, tags={HERB=true}}, CanonicalItem{name=Red Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}, CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, CanonicalItem{name=Celery Sticks, parent=CanonicalItem{name=Celery, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}}, CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}}, CanonicalItem{name=tomato puree}, CanonicalItem{name=Bacon Rashers, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Carrots, tags={VEGETABLE=true}}, CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}]";
		assertThat( Correlations.findCountsWith( CanonicalItemFactory.get("olive oil").get(), CanonicalItemFactory.get("Garlic").get(), CanonicalItemFactory.get("Onions").get()).toString(), is(expected));

		final Multiset<ITag> withTags = Correlations.findTagsWith( CanonicalItemFactory.get("Coriander").get(), CanonicalItemFactory.get("Chicken Stock").get(), CanonicalItemFactory.get("Star Anise").get() , CanonicalItemFactory.get("Cumin Seeds").get());
		assertThat( withTags.toString(), is("[SPICE x 2, CHINESE, HERB, INDIAN, MEAT, SAUCE]"));

		final Multiset<ITag> withoutTags = Correlations.findTagsWithout( CanonicalItemFactory.get("Coriander").get() );
		assertThat( withoutTags.toString(), is("[VEGETABLE x 19, SPICE x 18, INDIAN x 14, MEAT x 13, CHINESE x 10, HERB x 7, ALCOHOL x 6, FAT x 6, SUGAR x 6, WINE x 6, CHILLI x 5, THAI x 5, DAIRY x 4, FLOUR x 4, ITALIAN x 4, OIL x 4, SAUCE x 4, CHEESE x 2, FRENCH x 2, FRUIT x 2, PASTA x 2, SEED x 2, VINEGAR x 2, EGG, NUT, SALT, SPANISH]"));
	}

	@AfterClass
	public void shutDown() {
		CanonicalItemFactory.stopES();
	}
}