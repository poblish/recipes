/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_PUTS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IForkDetails;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.tags.RecipeTags;
import uk.co.recipes.test.TestDataUtils;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeExploreRecommendTest {

    @Inject Client esClient;
    @Inject MetricRegistry metrics;
    @Inject IEventService events;

    @Inject EsItemFactory itemFactory;
    @Inject EsUserFactory userFactory;
    @Inject EsRecipeFactory recipeFactory;
    @Inject EsSequenceFactory sequenceFactory;
    @Inject ItemsLoader loader;

    @Inject TestDataUtils dataUtils;

    @Inject MyrrixUpdater updater;
    @Inject MyrrixExplorerService explorerApi;
    @Inject MyrrixRecommendationService recsApi;


    private void injectDependencies() {
		DaggerRecipeExploreRecommendTest_TestComponent.create().inject(this);
    }

	@BeforeClass
	public void cleanIndices() throws IOException {
        injectDependencies();

        updater.startListening();

	    userFactory.deleteAll();
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	    sequenceFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
	    loader.load();

		final IUser adminUser = userFactory.adminUser();

		int count = 0;

		for ( File each : new File("src/test/resources/ingredients").listFiles((dir, name) -> name.endsWith(".txt"))) {
			dataUtils.parseIngredientsFrom( adminUser, each.getName() );
			count++;
		}

        while ( recipeFactory.countAll() < count) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        assertThat( metrics.timer(TIMER_RECIPES_PUTS).getCount(), is((long) count));
	}

	@Test
	public void testExplorer() throws IOException, TasteException {
        runSimilarity("inputs3.txt");
        runSimilarity("bol1");
        runSimilarity("bol2");
        runSimilarity("chineseBeef");
        runSimilarity("chCashBlackSpiceCurry.txt");
        runSimilarity("Bulk");
	}

    private void runSimilarity( final String inName) throws IOException {
        final IRecipe item = recipeFactory.get(inName).get();
        assertThat( item.getTitle(), is(inName));
        assertThat( item.getLocale(), is( Locale.UK ));  // FIXME Test is sound, but shouldn't be here!!!
        System.out.println( Strings.padEnd("Similar to " + inName + ":", 28, ' ') + explorerApi.similarRecipes( item, 10) );
    }

	@Test
	public void testRecommendations() throws IOException, TasteException {
		final IUser user1 = userFactory.getOrCreate( "Andrew Regan", () -> new User( "aregan", "Andrew Regan"));

		assertThat( user1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final IUser user2 = userFactory.getOrCreate( "Foo Bar", () -> new User( "foobar", "Foo Bar"));

		assertThat( user2.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		events.rateRecipe( user1, recipeFactory.getByName("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getByName("bol2.txt"), (float) Math.random());
		events.rateRecipe( user1, recipeFactory.getByName("chineseBeef"), (float) Math.random());

		events.rateRecipe( user2, recipeFactory.getByName("inputs3.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("chcashblackspicecurry.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("bol1.txt"), (float) Math.random());
		events.rateRecipe( user2, recipeFactory.getByName("Bulk"), (float) Math.random());

		final List<IRecipe> recsFor1 = recsApi.recommendRecipes( user1, 20);
		final List<IRecipe> recsFor2 = recsApi.recommendRecipes( user2, 20);

		assertThat( recsFor1.size(), greaterThanOrEqualTo(2));
		assertThat( recsFor2.size(), greaterThanOrEqualTo(2));

		System.out.println("Recommendations.1: " + recsFor1);
		System.out.println("Recommendations.2: " + recsFor2);

		assertThat( recsFor1, hasItem( recipeFactory.getByName("Bulk")  ));
		assertThat( recsFor1, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));

		// FIXME 3/8/17 Temp broken? ... assertThat( recsFor2, hasItem( recipeFactory.getByName("bol2.txt")  ));
		assertThat( recsFor2, hasItem( recipeFactory.getByName("chineseBeef")  ));
		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("chcashblackspicecurry.txt")  )));
		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));
	}

    @Test
    public void testModifiedExploration() throws IOException, TasteException {
        final IRecipe recipe1 = recipeFactory.get("inputs3.txt").get();
        final long currNumRecipes = recipeFactory.countAll();

        System.out.println("Existing: " + recipe1);

        assertThat( explorerApi.similarity( recipe1, recipe1), is(1f));  // Check Recipe has 100% similarity to itself
 
        recipeFactory.useCopy( recipe1, inCopy -> {
            try {
                assertThat( inCopy.getTitle(), not("inputs3.txt"));
                assertThat( inCopy.getIngredients(), is( recipe1.getIngredients() ));
                assertThat( inCopy.getItems().size(), is(15));
                assertThat( inCopy.removeItems( item("ginger"), item("coriander"), item("onion"), item("cinnamon_stick"), item("turmeric") /*, item("fennel_seed") */ ), is(true));
                assertThat( inCopy.getItems().size(), is(10));  // Check Items have actually been removed!
            }
            catch (IOException e) {
                Throwables.propagate(e);
            }
        }, inCopy -> {
            assertThat( inCopy.getItems().size(), is(10));  // Check we've actually got the one where the Items were removed!
            assertThat( inCopy.getId(), not( recipe1.getId() ));  // Check newly persisted Recipe has different Id

            final IForkDetails forkDetails = inCopy.getForkDetails();
            assertThat( forkDetails.getOriginalId(), is( recipe1.getId() ));
            assertThat( forkDetails.getOriginalTitle(), is( recipe1.getTitle() ));
            assertThat( forkDetails.getOriginalUser(), is( recipe1.getCreator() ));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }

            assertThat( recipeFactory.countAll(), is( currNumRecipes + 1));  // Check # Recipes has gone up by 1
            System.out.println( explorerApi.similarity( recipe1, inCopy) );
            assertThat( explorerApi.similarity( recipe1, inCopy), lessThan(1f));  // Check Recipe has imperfect similarity to itself (could potentially be 100%, I guess...)
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

	@Test
	public void testTagPersistence() throws IOException {
		final User user = new User( "aregan", "Andrew R");
        final IRecipe recipe1 = new Recipe(user, "Lovely Food", Locale.UK);
        assertThat( recipe1.getTags().isEmpty(), is(true));

        recipe1.addTag( RecipeTags.RECIPE_CUISINE, "Hungarian");
        recipe1.addTag( RecipeTags.SERVES_COUNT, 2);
        recipe1.addTag( RecipeTags.RECIPE_CATEGORY, "Main Course");
        recipe1.addTag( RecipeTags.VEGAN );

        recipeFactory.put( recipe1, null);
        recipeFactory.waitUntilRefreshed();

        final IRecipe loaded = recipeFactory.get("Lovely Food").get();
        assertThat( loaded.getTags().toString(), is("{RECIPE_CATEGORY=Main Course, RECIPE_CUISINE=Hungarian, SERVES_COUNT=2, VEGAN=true}"));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}

	@Singleton
	@Component(modules={ DaggerModule.class })
	public interface TestComponent {
		void inject(final RecipeExploreRecommendTest runner);
	}
}