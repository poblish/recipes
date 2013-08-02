/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IItemPersistence;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.test.TestDataUtils;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeSearchTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );

	private ISearchAPI searchApi = GRAPH.get( EsSearchService.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();

		dataUtils.parseIngredientsFrom("inputs3.txt");
		dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		dataUtils.parseIngredientsFrom("bol1.txt");
		dataUtils.parseIngredientsFrom("bol2.txt");
		dataUtils.parseIngredientsFrom("chineseBeef.txt");

        while ( recipeFactory.countAll() < 5) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
	}

	@Test
	public void findGarlicRecipes() throws InterruptedException, IOException {
		assertThat( searchApi.countRecipesByName("garlic"), is(4));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}
