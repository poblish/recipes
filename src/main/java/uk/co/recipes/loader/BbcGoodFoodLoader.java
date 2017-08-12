package uk.co.recipes.loader;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Throwables;
import dagger.Component;
import org.cfg4j.provider.ConfigurationProvider;
import org.elasticsearch.client.Client;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.ProductionMyrrixModule;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.*;
import uk.co.recipes.test.TestDataUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_DIR_PROCESS;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_PUTS;

/**
 * TODO
 *
 * @author andrewregan
 */
public class BbcGoodFoodLoader {

    // private static final Logger LOG = LoggerFactory.getLogger( BbcGoodFoodLoader.class );

    @Inject
    EsUserFactory userFactory;
    @Inject
    EsItemFactory itemFactory;
    @Inject
    EsRecipeFactory recipeFactory;
    @Inject
    EsSequenceFactory sequenceFactory;
    @Inject
    Client esClient;
    @Inject
    ItemsLoader loader;
    @Inject
    TestDataUtils dataUtils;
    @Inject
    MyrrixUpdater updater;
    @Inject
    MetricRegistry metrics;
    @Inject
    ConfigurationProvider config;

    public static void main(String[] args) {
        try {
            long st = System.currentTimeMillis();

            final BbcGoodFoodLoader loader = new BbcGoodFoodLoader();

            DaggerBbcGoodFoodLoader_AppComponent.create().inject(loader);

            loader.start(true);

            int i = 0;
            while (i++ < 7) {
                System.out.println("Waiting to finish...");
                Thread.sleep(1000);  // Pretty lame
            }

            System.out.println("Finished loading in " + ((System.currentTimeMillis() - st) / 1000d) + " msecs");
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    @Inject
    public BbcGoodFoodLoader() {
        // For Dagger
    }

    public void start(boolean inClearData) throws IOException, InterruptedException {
        updater.startListening();

        if (inClearData) {
            userFactory.deleteAll();
            itemFactory.deleteAll();
            recipeFactory.deleteAll();
            sequenceFactory.deleteAll();
        }

        loadIngredientsFromYaml();

        shutDown();
    }

    public void loadIngredientsFromYaml() throws InterruptedException, IOException {
        loader.load();

        final IUser adminUser = userFactory.adminUser();

        int count = 0;
//		int errors = 0;

        final File path = config.getProperty("loader.bbcgoodfood.path", File.class);

        try (Context ctxt = metrics.timer(TIMER_RECIPES_DIR_PROCESS + ":bbcGoodFood").time()) {
            for (File each : path.listFiles((dir, name) -> name.endsWith(".txt"))) {
                try {
                    dataUtils.parseIngredientsFrom(adminUser, path, each.getName());
                    count++;
                } catch (RuntimeException e) {
                    System.err.println(e);
                    // FIXME: use LOG.error("", e);
                    //					errors++;
                }
            }
        }

        while (recipeFactory.countAll() < count) {
            Thread.sleep(200); // Wait for saves to appear...
        }

        assertThat(metrics.timer(TIMER_RECIPES_PUTS).getCount(), is((long) count));
    }

    public void shutDown() {
//		esClient.close();
    }

    @Singleton
    @Component(modules = {DaggerModule.class, ProductionMyrrixModule.class})
    public interface AppComponent {
        void inject(final BbcGoodFoodLoader runner);
    }
}
