/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringReader;

import net.myrrix.client.ClientRecommender;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;

import com.google.common.base.Supplier;

import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeSearchTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private EsItemFactory itemFactory = GRAPH.get( EsItemFactory.class );
	private RecipeFactory recipeFactory = GRAPH.get( RecipeFactory.class );
	private EsUserFactory userFactory = GRAPH.get( EsUserFactory.class );

	private ISearchAPI searchApi = GRAPH.get( EsSearchService.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );
	private IRecommendationsAPI recsApi = GRAPH.get( MyrrixRecommendationService.class );

	private ClientRecommender recommender = GRAPH.get( ClientRecommender.class );

	private MyrrixUpdater myrrixUpdater = GRAPH.get( MyrrixUpdater.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
	    myrrixUpdater.startListening();

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
	public void testExplorer() throws IOException, TasteException {
		final IRecipe recipe1 = recipeFactory.getById("chcashblackspicecurry.txt");
		assertThat( recipe1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final StringReader sr = new StringReader("1," + recipe1.getId() + "");
		recommender.ingest(sr);
		recommender.refresh();

		System.out.println( explorerApi.similarRecipes( recipe1, 10) );
	}

	@Test
	public void testRecommendations() throws IOException, TasteException {
		final IUser user1 = userFactory.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User();
			}
		} );

		assertThat( user1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final StringBuffer buf = new StringBuffer( user1.getId() + "," + getRecipeIdByName("inputs3.txt") + "," + Math.random() + "\n");
		buf.append( user1.getId() + "," + getRecipeIdByName("chcashblackspicecurry.txt") + "," + Math.random() + "\n");
		buf.append( user1.getId() + "," + getRecipeIdByName("bol1.txt") + "," + Math.random() + "\n");
		buf.append( user1.getId() + "," + getRecipeIdByName("bol2.txt") + "," + Math.random() + "\n");
		buf.append( user1.getId() + "," + getRecipeIdByName("chinesebeef.txt") + "," + Math.random() + "\n");

		recommender.ingest( new StringReader( buf.toString() ) );
		recommender.refresh();

		System.out.println( recsApi.recommendRecipes( user1, 100) );
	}

	@Test
	public void findGarlicRecipes() throws InterruptedException, IOException {
		assertThat( searchApi.countRecipesByName("garlic"), is(4));
	}

	private long getRecipeIdByName( final String inName) throws IOException {
		final IRecipe r = recipeFactory.getById(inName);
		assertThat( r.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted
		return r.getId();
	}

	@AfterClass
	public void shutDown() {
		itemFactory.stopES();
	}
}
