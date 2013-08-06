/**
 * 
 */
package uk.co.recipes;

import dagger.ObjectGraph;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * @author andrewr
 *
 */
public class UserPersistenceTest {

    private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

    private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
    private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
    private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
    private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );

    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );


    @BeforeClass
    public void cleanIndices() throws ClientProtocolException, IOException {
        updater.startListening();

        itemFactory.deleteAll();
        sequenceFactory.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws IOException {
        GRAPH.get( ItemsLoader.class ).load();
    }

    @Test
    public void testPersistence() throws IOException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User( testUName, testDName);
        u1.addRating( new ItemRating( itemFactory.get("ginger").get(), 8) );
        u1.addRating( new RecipeRating( recipeFactory.get("venisonBurgundy.txt").get(), 6) );
        assertThat( u1.getItemRatings().size(), is(1));
        assertThat( u1.getRecipeRatings().size(), is(1));

        userFactory.put( u1, userFactory.toStringId(u1));

        final IUser retrievedUser = userFactory.getById( userFactory.toStringId(u1) );
        assertThat( newHashSet( u1.getItemRatings() ), is( newHashSet( retrievedUser.getItemRatings() ) ));
        assertThat( newHashSet( u1.getRecipeRatings() ), is( newHashSet( retrievedUser.getRecipeRatings() ) ));
    }
}