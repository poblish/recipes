/**
 * 
 */
package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import net.myrrix.client.ClientRecommender;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.mocks.MockFactories;
import uk.co.recipes.myrrix.MyrrixPrefsIngester;
import uk.co.recipes.persistence.EsItemFactory;
import dagger.Module;
import dagger.Provides;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixPrefsIngestTest {

	@Inject MyrrixPrefsIngester ingester;

	@BeforeClass
	private void injectDependencies() throws IOException {
        DaggerMyrrixPrefsIngestTest_TestComponent.builder().testModule( new TestModule() ).build().inject(this);
	}

    @Test
    public void testLoadScores() throws IOException {
        final String contents = ingester.parseRecommendations( new File("src/test/resources/recommendations1.yaml") );
        assertThat( contents, is("2,1510281992,1\r2,461905405,3\r2,467117389,10\r2,2133018664,10\r2,632006359,8\r2,2360810,5\r3,2044784730,8\r3,1859661278,5\r3,779718024,1\r3,2066500,3"));
    }

    @Test
    public void testLoadFaves() throws IOException {
        final Collection<ICanonicalItem> faves = ingester.parseFaves( new File("src/test/resources/recommendations1.yaml") );
        assertThat( faves.toString(), is("[CanonicalItem{name=Coriander Seeds}, CanonicalItem{name=Garam Masala}]"));
    }

    @Test
    public void testLoadBlocks() throws IOException {
        final String contents = ingester.parseBlocks( new File("src/test/resources/recommendations1.yaml") );
        assertThat( contents, is("ok"));
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
        ClientRecommender provideClientRecommender() {
            return mock(ClientRecommender.class);  // Mockito 2.x
        }
    }

    @Singleton
    @Component(modules={ TestModule.class })
    public interface TestComponent {
        void inject(final MyrrixPrefsIngestTest runner);
    }
}
