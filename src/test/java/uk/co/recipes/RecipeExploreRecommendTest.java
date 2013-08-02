/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.test.TestDataUtils;

import com.google.common.base.Strings;
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

	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
	private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );
	

	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );
	private IRecommendationsAPI recsApi = GRAPH.get( MyrrixRecommendationService.class );

    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );
	private IEventService events = GRAPH.get( IEventService.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		updater.startListening();

	    userFactory.deleteAll();
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	    sequenceFactory.deleteAll();
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

        while ( recipeFactory.countAll() < count) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
	}

	@Test
	public void testExplorer() throws IOException, TasteException {
        runSimilarity("inputs3.txt");
        runSimilarity("bol1.txt");
        runSimilarity("bol2.txt");
        runSimilarity("chineseBeef.txt");
        runSimilarity("chCashBlackSpiceCurry.txt");
        runSimilarity("bulk.txt");
	}

    private void runSimilarity( final String inName) throws IOException {
        final IRecipe item = recipeFactory.get(inName).get();
        assertThat( inName, is( item.getTitle() ));
        System.out.println( Strings.padEnd("Similar to " + inName + ":", 28, ' ') + explorerApi.similarRecipes( item, 10) );
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

		final IUser user2 = userFactory.getOrCreate( "Foo Bar", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User();
			}
		} );

		assertThat( user2.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		events.rateRecipe( user1, recipeFactory.getById("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("bol2.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getById("chinesebeef.txt"), (float) Math.random());

		events.rateRecipe( user2, recipeFactory.getById("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getById("chcashblackspicecurry.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getById("bol1.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getById("bulk.txt"), (float) Math.random());

		final List<IRecipe> recsFor1 = recsApi.recommendRecipes( user1, 20);
		final List<IRecipe> recsFor2 = recsApi.recommendRecipes( user2, 20);

		assertThat( recsFor1.size(), greaterThanOrEqualTo(2));
		assertThat( recsFor2.size(), greaterThanOrEqualTo(2));

		System.out.println("Recommendations.1: " + recsFor1);
		System.out.println("Recommendations.2: " + recsFor2);

		assertThat( recsFor1, hasItem( recipeFactory.getById("bulk.txt")  ));
		assertThat( recsFor1, not(hasItem( recipeFactory.getById("inputs3.txt")  )));

		assertThat( recsFor2, hasItem( recipeFactory.getById("bol2.txt")  ));
		assertThat( recsFor2, hasItem( recipeFactory.getById("chinesebeef.txt")  ));
		assertThat( recsFor2, not(hasItem( recipeFactory.getById("chcashblackspicecurry.txt")  )));
		assertThat( recsFor2, not(hasItem( recipeFactory.getById("inputs3.txt")  )));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}