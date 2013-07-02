/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.persistence.RecipeFactory;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeSearchTest {

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		CanonicalItemFactory.startES();
		CanonicalItemFactory.deleteAll();
		RecipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		ItemsLoader.load();

		parseIngredientsFrom("inputs3.txt");
		parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		parseIngredientsFrom("bol1.txt");
		parseIngredientsFrom("bol2.txt");
		parseIngredientsFrom("chineseBeef.txt");

        while ( RecipeFactory.listAll().size() < 5) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
	}

	@Test
	public void findGarlicRecipes() throws InterruptedException, IOException {
		final JsonNode jn = JacksonFactory.getMapper().readTree( new URL("http://localhost:9200/recipe/recipes/_search?q=canonicalName:garlic") ).path("hits").path("hits");
		assertThat( jn.size(), is(4));
	}

	@AfterClass
	public void shutDown() {
		CanonicalItemFactory.stopES();
	}
}
