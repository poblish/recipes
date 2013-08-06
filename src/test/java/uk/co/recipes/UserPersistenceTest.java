/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        userFactory.put( u1, userFactory.toStringId(u1));

        u1.addRating( new ItemRating( u1, itemFactory.get("ginger").get(), 8) );
        u1.addRating( new RecipeRating( u1, recipeFactory.get("inputs3.txt").get(), 6) );
        assertThat( u1.getRatings().size(), is(2));

        final IUser retrievedUser = userFactory.getById( userFactory.toStringId(u1) );
        assertThat( u1, is(retrievedUser));

        userFactory.deleteAll();
    }
}