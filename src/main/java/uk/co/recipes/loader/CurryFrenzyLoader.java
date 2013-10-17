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

import javax.inject.Inject;

import org.elasticsearch.client.Client;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.test.TestDataUtils;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;

import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CurryFrenzyLoader {

    @Inject EsRecipeFactory recipeFactory;
    @Inject Client esClient;
    @Inject ItemsLoader loader;
    @Inject TestDataUtils dataUtils;
    @Inject MyrrixUpdater updater;
    @Inject MetricRegistry metrics;

	public static void main( String[] args) {
		try {
			long st = System.currentTimeMillis();
			new CurryFrenzyLoader().start();

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
        ObjectGraph.create( new TestModule() ).inject(this);

        updater.startListening();

//	    userFactory.deleteAll();
//		itemFactory.deleteAll();
//		recipeFactory.deleteAll();
//	    sequenceFactory.deleteAll();

	    loadIngredientsFromYaml();

	    shutDown();
	}

	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		loader.load();

		int count = 0;
		int errors = 0;

		for ( File each : new File("src/test/resources/ingredients/curryfrenzy/").listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name) {
				return name.endsWith(".txt");
			}
		} )) {
			try
			{
				dataUtils.parseIngredientsFrom( "src/test/resources/ingredients/curryfrenzy/", each.getName() );
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

        System.out.println("> " + errors + " errors out of " + (count + errors));
	}

	public void shutDown() {
		esClient.close();
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=CurryFrenzyLoader.class)
    static class TestModule {}
}