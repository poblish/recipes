/**
 * 
 */
package uk.co.recipes.persistence;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

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

	@Test(enabled=false)
	public void loadIngredientsFromYaml() throws IOException {
		loader.load();
	}

	@Module( includes=DaggerModule.class, overrides=true, injects=ItemsLoaderTest.class)
	private static class TestModule {
		@Provides
		@Singleton
		EsItemFactory provideEsItemFactory() {
			final EsItemFactory mockIF = mock( EsItemFactory.class );
			Optional<ICanonicalItem>  x = Optional.absent();
			try {
			when( mockIF.get( anyString() ) ).thenReturn(x);
			}
			catch (IOException e) {
				Throwables.propagate(e);
			}
			return mockIF;
		}
	}
}
