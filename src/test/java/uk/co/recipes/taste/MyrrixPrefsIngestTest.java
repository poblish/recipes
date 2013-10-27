/**
 * 
 */
package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.mocks.MockFactories;
import uk.co.recipes.myrrix.MyrrixPrefsIngester;
import uk.co.recipes.persistence.EsItemFactory;
import dagger.Module;
import dagger.ObjectGraph;
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
	public void injectDependencies() throws ClientProtocolException, IOException {
        ObjectGraph.create( new TestModule() ).inject(this);
	}

	@Test
	public void testLoading() throws IOException {
		final String contents = ingester.parseRecommendations("src/test/resources/recommendations1.yaml");
		assertThat( contents, is("2,533277409,1\r2,2093110160,3\r2,467117389,10\r2,2133018664,10\r2,632006359,8\r2,2360810,5\r3,2044784730,8\r3,1736610420,5\r3,779718024,1\r3,2066500,3"));
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=MyrrixPrefsIngestTest.class)
    static class TestModule {

        @Provides
        @Singleton
        EsItemFactory provideItemFactory() {
        	return MockFactories.mockItemFactory();
        }
    }
}
