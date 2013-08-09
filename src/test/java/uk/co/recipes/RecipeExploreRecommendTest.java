/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.co.recipes.metrics.MetricNames.COUNTER_RECIPES_PUTS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
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

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

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
    private MetricRegistry metrics = GRAPH.get( MetricRegistry.class );

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

        assertThat( metrics.counter(COUNTER_RECIPES_PUTS).getCount(), is((long) count));
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
        assertThat( item.getLocale(), is( Locale.UK ));  // FIXME Test is sound, but shouldn't be here!!!
        System.out.println( Strings.padEnd("Similar to " + inName + ":", 28, ' ') + explorerApi.similarRecipes( item, 10) );
    }

	@Test
	public void testRecommendations() throws IOException, TasteException {
		final IUser user1 = userFactory.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "aregan", "Andrew Regan");
			}
		} );

		assertThat( user1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final IUser user2 = userFactory.getOrCreate( "Foo Bar", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "foobar", "Foo Bar");
			}
		} );

		assertThat( user2.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		events.rateRecipe( user1, recipeFactory.getByName("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getByName("bol2.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getByName("chinesebeef.txt"), (float) Math.random());

		events.rateRecipe( user2, recipeFactory.getByName("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("chcashblackspicecurry.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("bol1.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("bulk.txt"), (float) Math.random());

		final List<IRecipe> recsFor1 = recsApi.recommendRecipes( user1, 20);
		final List<IRecipe> recsFor2 = recsApi.recommendRecipes( user2, 20);

		assertThat( recsFor1.size(), greaterThanOrEqualTo(2));
		assertThat( recsFor2.size(), greaterThanOrEqualTo(2));

		System.out.println("Recommendations.1: " + recsFor1);
		System.out.println("Recommendations.2: " + recsFor2);

		assertThat( recsFor1, hasItem( recipeFactory.getByName("bulk.txt")  ));
		assertThat( recsFor1, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));

		assertThat( recsFor2, hasItem( recipeFactory.getByName("bol2.txt")  ));
		assertThat( recsFor2, hasItem( recipeFactory.getByName("chinesebeef.txt")  ));
		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("chcashblackspicecurry.txt")  )));
		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));
	}

    @Test
    public void testModifiedExploration() throws IOException, TasteException {
        final IRecipe recipe1 = recipeFactory.get("inputs3.txt").get();
        final long currNumRecipes = recipeFactory.countAll();

        System.out.println("Existing: " + recipe1);

        assertThat( explorerApi.similarity( recipe1, recipe1), is(1f));  // Check Recipe has 100% similarity to itself
 
        ((EsRecipeFactory) recipeFactory).useCopy( recipe1, new EsRecipeFactory.PreForkChange<IRecipe>() {

			@Override
			public void apply( final IRecipe inCopy) {
				try {
                    assertThat( inCopy.getTitle(), not("inputs3.txt"));
                    assertThat( inCopy.getIngredients(), is( recipe1.getIngredients() ));
                    assertThat( inCopy.getItems().size(), is(15));
					assertThat( inCopy.removeItems( item("ginger"), item("coriander"), item("onion"), item("cinnamon_stick"), item("turmeric"), item("fennel_seed")), is(true));
					assertThat( inCopy.getItems().size(), is(9));  // Check Items have actually been removed!
				}
				catch (IOException e) {
					Throwables.propagate(e);
				}
			}
			
        }, new EsRecipeFactory.PostForkChange<IRecipe>() {

            @Override
            public void apply( final IRecipe inCopy) throws IOException {
				assertThat( inCopy.getItems().size(), is(9));  // Check we've actually got the one where the Items were removed!
                assertThat( inCopy.getId(), not( recipe1.getId() ));  // Check newly persisted Recipe has different Id

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Throwables.propagate(e);
                }

                assertThat( recipeFactory.countAll(), is( currNumRecipes + 1));  // Check # Recipes has gone up by 1
                System.out.println( explorerApi.similarity( recipe1, inCopy) );
                assertThat( explorerApi.similarity( recipe1, inCopy), lessThan(1f));  // Check Recipe has imperfect similarity to itself (could potentially be 100%, I guess...)
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        assertThat( recipeFactory.countAll(), is(currNumRecipes));  // Check # Recipes is back where it was
        assertThat( recipeFactory.get("inputs3.txt").isPresent(), is(true));  // Check the original Recipe wasn't deleted, i.e. new one was
    }

    private ICanonicalItem item( final String inName) throws IOException {
    	return itemFactory.get(inName).get();
    }

	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}