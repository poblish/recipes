/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;

import net.myrrix.client.ClientRecommender;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IRecommendationsAPI;
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
public class RecipeExploreRecommendTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );

	private EsItemFactory itemFactory = GRAPH.get( EsItemFactory.class );
	private EsRecipeFactory recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private EsUserFactory userFactory = GRAPH.get( EsUserFactory.class );

	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );
	private IRecommendationsAPI recsApi = GRAPH.get( MyrrixRecommendationService.class );

	private ClientRecommender recommender = GRAPH.get( ClientRecommender.class );

	private MyrrixUpdater myrrixUpdater = GRAPH.get( MyrrixUpdater.class );

	private IEventService events = GRAPH.get( IEventService.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
	    myrrixUpdater.startListening();

		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();

		int count = 0;

		for ( File each : new File("src/test/resources/ingredients").listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name) {
				return name.endsWith(".txt");
			}
		} )) {
			dataUtils.parseIngredientsFrom( each.getName() );
			count++;
		}

        while ( recipeFactory.listAll().size() < count) {
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

		System.out.println("Similar: " + explorerApi.similarRecipes( recipe1, 10) );
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

		events.rateRecipe( user1, recipeFactory.getById("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("chcashblackspicecurry.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("bol1.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("bol2.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("chinesebeef.txt"), (float) Math.random());

		System.out.println("Recommendations: " + recsApi.recommendRecipes( user1, 100) );
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}