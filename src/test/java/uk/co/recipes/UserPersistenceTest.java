/**
 * 
 */
package uk.co.recipes;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.test.TestDataUtils;
import dagger.ObjectGraph;


/**
 * @author andrewr
 *
 */
public class UserPersistenceTest {

    private final static String TEST_RECIPE = "inputs3.txt";

    private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

    private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
    private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
    private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
    private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );

	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );
	private UserRatings userRatings = GRAPH.get( UserRatings.class );


    @BeforeClass
    public void cleanIndices() throws ClientProtocolException, IOException {
        userFactory.deleteAll();
        sequenceFactory.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws IOException, InterruptedException {
        GRAPH.get( ItemsLoader.class ).load();

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
        u1.getPrefs().explorerInclude( CommonTags.VEGETABLE );
        u1.getPrefs().explorerExclude( CommonTags.MEAT );
        userFactory.put( u1, userFactory.toStringId(u1));

        userFactory.waitUntilRefreshed();

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat( retrievedUser.getPrefs().getExplorerIncludeTags(), hasItem( CommonTags.VEGETABLE ));
        assertThat( retrievedUser.getPrefs().getExplorerExcludeTags(), hasItem( CommonTags.MEAT ));
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
        userRatings.addRating( u1, new ItemRating( itemFactory.get("ginger").get(), 8) );
        userRatings.addRating( u1, new RecipeRating( recipeFactory.get(TEST_RECIPE).get(), 6) );
        assertThat( u1.getItemRatings().size(), is(1));
        assertThat( u1.getRecipeRatings().size(), is(1));

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat( newHashSet( u1.getItemRatings() ), is( newHashSet( retrievedUser.getItemRatings() ) ));
        assertThat( newHashSet( u1.getRecipeRatings() ), is( newHashSet( retrievedUser.getRecipeRatings() ) ));

        userRatings.addRating( retrievedUser, new ItemRating( itemFactory.get("turmeric").get(), 7) );
        assertThat( retrievedUser.getItemRatings().size(), is(2));
        assertThat( retrievedUser.getRecipeRatings().size(), is(1));
    }
}