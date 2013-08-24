/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_PUTS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.test.TestDataUtils;

import com.codahale.metrics.MetricRegistry;

import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class BbcGoodFoodLoaderTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
    private MetricRegistry metrics = GRAPH.get( MetricRegistry.class );

	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
	private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );
	
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );
    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		updater.startListening();

	    userFactory.deleteAll();
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	    sequenceFactory.deleteAll();
	}

	@Test
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();

		int count = 0;
		int errors = 0;

		for ( File each : new File("src/test/resources/ingredients/bbcgoodfood/").listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name) {
				return name.endsWith(".txt");
			}
		} )) {
			try
			{
				dataUtils.parseIngredientsFrom( "src/test/resources/ingredients/bbcgoodfood/", each.getName() );
				count++;
			}
			catch (RuntimeException e) {
				System.err.println(e);
				errors++;
			}
		}

        while ( recipeFactory.countAll() < count) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        assertThat( metrics.timer(TIMER_RECIPES_PUTS).getCount(), is((long) count));
	}
	@AfterClass
	public void shutDown() {
		esClient.close();
	}
}