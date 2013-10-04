/**
 * 
 */
package uk.co.recipes;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IUser;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.MeatAndFishTags;
import uk.co.recipes.test.TestDataUtils;

import com.google.common.base.Optional;

import dagger.Module;
import dagger.ObjectGraph;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class UserPersistenceTest {

    private final static String TEST_RECIPE = "inputs3.txt";

    @Inject EsItemFactory itemFactory;
    @Inject EsUserFactory userFactory;
    @Inject EsRecipeFactory recipeFactory;
    @Inject EsSequenceFactory sequenceFactory;
    @Inject TestDataUtils dataUtils;
    @Inject UserRatings userRatings;
    @Inject ItemsLoader loader;

    private void injectDependencies() {
        ObjectGraph.create( new TestModule() ).inject(this);
    }

    @BeforeClass
    public void cleanIndices() throws ClientProtocolException, IOException {
        injectDependencies();

        userFactory.deleteAll();
        sequenceFactory.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws IOException, InterruptedException {
        loader.load();
		dataUtils.parseIngredientsFrom(TEST_RECIPE);

        while ( recipeFactory.countAll() < 1) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
    }

    @Test
    public void testPersistenceWithPrefs() throws IOException, InterruptedException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User( testUName, testDName);
        u1.getPrefs().explorerIncludeAdd( CommonTags.VEGETABLE );
        u1.getPrefs().explorerExcludeAdd( MeatAndFishTags.MEAT );
        userFactory.put( u1, userFactory.toStringId(u1));

        userFactory.waitUntilRefreshed();

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat( retrievedUser.getPrefs().getExplorerIncludeTags(), hasItem( CommonTags.VEGETABLE ));
        assertThat( retrievedUser.getPrefs().getExplorerExcludeTags(), hasItem( MeatAndFishTags.MEAT ));
    }

    @Test
    public void testPersistenceWithAuth() throws IOException, InterruptedException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User( testUName, testDName);
        u1.addAuth( new UserAuth( "google", "12345") );
        u1.addAuth( new UserAuth( "google", "56789") );
        u1.removeAuth( new UserAuth( "google", "12345") );
        assertThat( u1.getAuths().size(), is(1));
        assertThat( u1.getAuths().iterator().next().getAuthId(), is("56789"));
        userFactory.put( u1, userFactory.toStringId(u1));

        userFactory.waitUntilRefreshed();

        assertThat( userFactory.findWithAuth( u1.getAuths().iterator().next() ).isPresent(), is(true));

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat( retrievedUser.isActive(), is(true));
        assertThat( newHashSet( u1.getAuths() ), is( newHashSet( retrievedUser.getAuths() ) ));
    }

    @Test
    public void testPersistenceWithRatings() throws IOException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User( testUName, testDName);
        userFactory.put( u1, userFactory.toStringId(u1));
        userRatings.addRating( u1, new ItemRating( itemFactory.get("ginger").get(), 5) );
        Optional<IItemRating> oldGingerRating = userRatings.addRating( u1, new ItemRating( itemFactory.get("ginger").get(), 8) );  // Actually, no, change that to 8...
        assertThat( u1.getItemRatings().size(), is(1));
        assertThat( u1.getItemRatings().iterator().next().getScore(), is(8));
        assertThat( oldGingerRating.get().getScore(), is(5));

        userRatings.addRating( u1, new RecipeRating( recipeFactory.get(TEST_RECIPE).get(), 1) );
        Optional<IRecipeRating> oldTestRecipeRating =  userRatings.addRating( u1, new RecipeRating( recipeFactory.get(TEST_RECIPE).get(), 6) );  // Actually, no, change that to 6...
        assertThat( u1.getRecipeRatings().size(), is(1));
        assertThat( u1.getRecipeRatings().iterator().next().getScore(), is(6));
        assertThat( oldTestRecipeRating.get().getScore(), is(1));

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat( newHashSet( u1.getItemRatings() ), is( newHashSet( retrievedUser.getItemRatings() ) ));
        assertThat( newHashSet( u1.getRecipeRatings() ), is( newHashSet( retrievedUser.getRecipeRatings() ) ));

        userRatings.addRating( retrievedUser, new ItemRating( itemFactory.get("turmeric").get(), 7) );
        assertThat( retrievedUser.getItemRatings().size(), is(2));
        assertThat( retrievedUser.getRecipeRatings().size(), is(1));
    }

    @Module( includes=DaggerModule.class, overrides=true, injects=UserPersistenceTest.class)
    static class TestModule {}
}