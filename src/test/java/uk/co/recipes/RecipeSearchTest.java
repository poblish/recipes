/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.MeatAndFishTags;
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
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );

	private EsExplorerFilters explorerFilters = GRAPH.get( EsExplorerFilters.class );

	private ISearchAPI searchApi = GRAPH.get( EsSearchService.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		GRAPH.get( EsItemFactory.class ).deleteAll();
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

	@Test
	public void testUnusedTag() throws IOException {
		assertThat( searchApi.findRecipesByTag( CommonTags.CITRUS ).size(), is(0));
	}

	@Test
	public void testSearchByTagAndFilters() throws IOException {
		final int numItems = searchApi.findItemsByTag( MeatAndFishTags.MEAT ).size();

		final List<IRecipe> foundRecipes = searchApi.findRecipesByTag( MeatAndFishTags.MEAT );
		assertThat( foundRecipes.size(), greaterThanOrEqualTo(4));  // Surely 5 ?!?

		final IExplorerFilter filter = explorerFilters.build().includeTags( MeatAndFishTags.MEAT ).toFilter();
		assertThat( filter.idsToInclude().length, is( foundRecipes.size() + numItems));
		assertThat( filter.idsToExclude().length, is(0));

		final IExplorerFilter filter2 = explorerFilters.build().excludeTags( MeatAndFishTags.MEAT ).toFilter();
		assertThat( filter2.idsToInclude().length, is(0));
		assertThat( filter2.idsToExclude().length, is( foundRecipes.size() + numItems));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}
