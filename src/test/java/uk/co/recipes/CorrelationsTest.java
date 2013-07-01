/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
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
        final String expected = "[CanonicalItem{name=Mushrooms, tags={VEGETABLE=true}} x 2, CanonicalItem{name=Carrots, tags={VEGETABLE=true}} x 2, CanonicalItem{name=Bay Leaf, tags={HERB=true}} x 2, CanonicalItem{name=Red Wine, tags={WINE=true, ALCOHOL=true}} x 2, CanonicalItem{name=Sugar, tags={SUGAR=true}}, CanonicalItem{name=Allspice, tags={SPICE=true}}, CanonicalItem{name=Cloves, tags={SPICE=true, INDIAN=true}}, CanonicalItem{name=Butter, tags={FAT=true, DAIRY=true}}, CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, tags={WINE=true, ALCOHOL=true, FRENCH=true}}, CanonicalItem{name=Flour, tags={FLOUR=true}}, CanonicalItem{name=Red Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}, CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, CanonicalItem{name=Rosemary, tags={HERB=true}}, CanonicalItem{name=Garlic Cloves, parent=CanonicalItem{name=Garlic, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Beef Stock, tags={MEAT=true, SAUCE=true}}, CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, CanonicalItem{name=Celery Sticks, parent=CanonicalItem{name=Celery, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Tomatoes, tags={VEGETABLE=true}}, CanonicalItem{name=tomato puree}, CanonicalItem{name=Salt, tags={SALT=true}}, CanonicalItem{name=pounds venison chuck}, CanonicalItem{name=Onion, tags={VEGETABLE=true}}, CanonicalItem{name=Streaky Bacon, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Thyme, tags={HERB=true}}, CanonicalItem{name=Minced Beef, parent=CanonicalItem{name=Beef, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Parsley, tags={HERB=true}}, CanonicalItem{name=Bacon Rashers, parent=CanonicalItem{name=Bacon, tags={MEAT=true}}, tags={MEAT=true}}, CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, CanonicalItem{name=Parmesan, tags={DAIRY=true, CHEESE=true, ITALIAN=true}}]";
		assertThat( Correlations.findCountsWith( CanonicalItemFactory.get("olive oil").get(), CanonicalItemFactory.get("Garlic").get(), CanonicalItemFactory.get("Onions").get()).toString(), is(expected));

		final Multiset<ITag> withTags = Correlations.findTagsWith( CanonicalItemFactory.get("Coriander").get(), CanonicalItemFactory.get("Chicken Stock").get(), CanonicalItemFactory.get("Star Anise").get() , CanonicalItemFactory.get("Cumin Seeds").get());
		assertThat( withTags.toString(), is("[CHINESE, HERB, INDIAN, MEAT, SAUCE, SPICE x 2]"));

		final Multiset<ITag> withoutTags = Correlations.findTagsWithout( CanonicalItemFactory.get("Coriander").get() );
		assertThat( withoutTags.toString(), is("[ALCOHOL x 6, CHEESE x 2, CHILLI x 5, CHINESE x 10, DAIRY x 4, EGG, FAT x 6, FLOUR x 4, FRENCH x 2, FRUIT x 2, HERB x 7, INDIAN x 14, ITALIAN x 3, MEAT x 13, NUT, OIL x 4, PASTA, SALT, SAUCE x 4, SEED x 2, SPANISH, SPICE x 18, SUGAR x 6, THAI x 5, VEGETABLE x 18, VINEGAR x 2, WINE x 6]"));
	}
}