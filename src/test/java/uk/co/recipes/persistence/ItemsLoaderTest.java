/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.mocks.MockFactories;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ItemsLoaderTest {

	@Inject ItemsLoader loader;
	@Inject EsItemFactory itemFactory;

	@BeforeClass public void setUp() {
		ObjectGraph.create( new TestModule() ).inject(this);
	}

	@Test
	public void loadIngredientsFromYaml() throws IOException {
		loader.load();
	}

	@Module( includes=DaggerModule.class, overrides=true, injects=ItemsLoaderTest.class)
	static class TestModule {
		@Provides
		@Singleton
		EsItemFactory provideEsItemFactory() {
			return MockFactories.inMemoryItemFactory();
		}
	}
}
