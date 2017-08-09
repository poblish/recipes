package uk.co.recipes.loader;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Throwables;
import dagger.Component;
import org.cfg4j.provider.ConfigurationProvider;
import org.elasticsearch.client.Client;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
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
public class CurryFrenzyLoader {

    @Inject
    EsUserFactory userFactory;
    @Inject
    EsRecipeFactory recipeFactory;
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

            final CurryFrenzyLoader loader = new CurryFrenzyLoader();

            DaggerCurryFrenzyLoader_AppComponent.create().inject(loader);

            loader.start();

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
    public CurryFrenzyLoader() {
        // For Dagger
    }

    public void start() throws IOException, InterruptedException {
        updater.startListening();

        loadIngredientsFromYaml();

        shutDown();
    }

    public void loadIngredientsFromYaml() throws InterruptedException, IOException {
        loader.load();

        final IUser adminUser = userFactory.adminUser();

        int count = 0;
        int errors = 0;

        final File path = config.getProperty("loader.curryfrenzy.path", File.class);

        try (Context ctxt = metrics.timer(TIMER_RECIPES_DIR_PROCESS + ":curryFrenzy").time()) {
            for (File each : path.listFiles((dir, name) -> name.endsWith(".txt"))) {
                try {
                    dataUtils.parseIngredientsFrom(adminUser, path, each.getName());
                    count++;
                } catch (RuntimeException e) {
                    System.err.println(e);
                    errors++;
                }
            }
        }

        while (recipeFactory.countAll() < count) {
            Thread.sleep(200); // Wait for saves to appear...
        }

        assertThat(metrics.timer(TIMER_RECIPES_PUTS).getCount(), is((long) count));

        System.out.println("> " + errors + " errors out of " + (count + errors));
    }

    public void shutDown() {
//		esClient.close();
    }

    @Singleton
    @Component(modules = {DaggerModule.class})
    public interface AppComponent {

        void inject(final CurryFrenzyLoader runner);
    }
}
