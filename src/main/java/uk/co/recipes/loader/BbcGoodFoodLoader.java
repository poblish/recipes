/**
 * 
 */
package uk.co.recipes.loader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_PUTS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.elasticsearch.client.Client;

import uk.co.recipes.DaggerModule;
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
import com.google.common.base.Throwables;

import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class BbcGoodFoodLoader {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
    private MetricRegistry metrics = GRAPH.get( MetricRegistry.class );

	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
	private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );
	
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );
    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );


	public static void main( String[] args) {
		try {
			long st = System.currentTimeMillis();
			new BbcGoodFoodLoader().start();

			int i = 0;
			while ( i++ < 7) {
				System.out.println("Waiting to finish...");
				Thread.sleep(1000);  // Pretty lame
			}

			System.out.println("Finished loading in " + (( System.currentTimeMillis() - st) / 1000d) + " msecs");
			System.exit(0);
		}
		catch (IOException e) {
			Throwables.propagate(e);
		} catch (InterruptedException e) {
			Throwables.propagate(e);
		}
	}

	public void start() throws IOException, InterruptedException {
		updater.startListening();

	    userFactory.deleteAll();
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	    sequenceFactory.deleteAll();

	    loadIngredientsFromYaml();

	    shutDown();
	}

	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();

		int count = 0;
//		int errors = 0;

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
//				errors++;
			}
		}

        while ( recipeFactory.countAll() < count) {
        	Thread.sleep(200); // Wait for saves to appear...
        }

        assertThat( metrics.timer(TIMER_RECIPES_PUTS).getCount(), is((long) count));
	}

	public void shutDown() {
		esClient.close();
	}
}