/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeSearchTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private CanonicalItemFactory itemFactory = GRAPH.get( CanonicalItemFactory.class );
	private RecipeFactory recipeFactory = GRAPH.get( RecipeFactory.class );

	private ObjectMapper mapper = GRAPH.get( ObjectMapper.class );
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

        while ( recipeFactory.listAll().size() < 5) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
	}

	@Test
	public void findGarlicRecipes() throws InterruptedException, IOException {
		assertThat( searchApi.findRecipesByName("garlic").size(), is(4));
	}

	@AfterClass
	public void shutDown() {
		itemFactory.stopES();
	}
}
