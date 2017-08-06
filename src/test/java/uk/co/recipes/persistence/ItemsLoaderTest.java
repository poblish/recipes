/**
 *
 */
package uk.co.recipes.persistence;

import com.codahale.metrics.MetricRegistry;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.mocks.MockFactories;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author andrewregan
 */
public class ItemsLoaderTest {

    @Inject
    ItemsLoader loader;
    @Inject
    EsItemFactory itemFactory;

    @BeforeClass
    public void injectDependencies() throws IOException {
        DaggerItemsLoaderTest_TestComponent.builder().testModule(new TestModule()).build().inject(this);
    }

    @Test(enabled = false)  // (AGR) 5/8/17 Not sure what purpose of this is any more
    public void loadIngredientsFromYaml() throws IOException {
        loader.load();
    }

    // FIXME Suboptimal: https://google.github.io/dagger/testing.html
    @Module
    public class TestModule extends DaggerModule {
        @Provides
        @Singleton
        EsItemFactory provideItemFactory() {
            return MockFactories.inMemoryItemFactory();
        }

        @Provides
        @Singleton
        MetricRegistry provideMetricRegistry() {
            return new MetricRegistry();
        }

        @Provides
        @Singleton
        @Named("prefixAdjustments")
        List<String> providePrefixAdjustments() {
            return Collections.emptyList();
        }

        @Provides
        @Singleton
        @Named("suffixAdjustments")
        List<String> provideSuffixAdjustments() {
            return Collections.emptyList();
        }
    }

    @Singleton
    @Component(modules = {TestModule.class})
    public interface TestComponent {
        void inject(final ItemsLoaderTest runner);

    }
}
