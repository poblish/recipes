/**
 * 
 */
package uk.co.recipes.taste;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import java.io.File;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
    public void testLoadScores() throws IOException {
        final String contents = ingester.parseRecommendations( new File("src/test/resources/recommendations1.yaml") );
        assertThat( contents, is("2,1510281992,1\r2,461905405,3\r2,467117389,10\r2,2133018664,10\r2,632006359,8\r2,2360810,5\r3,2044784730,8\r3,1859661278,5\r3,779718024,1\r3,2066500,3"));
    }

    @Test
    public void testLoadFaves() throws IOException {
        final String contents = ingester.parseFaves( new File("src/test/resources/recommendations1.yaml") );
        assertThat( contents, is("ok"));
    }

    @Test
    public void testLoadBlocks() throws IOException {
        final String contents = ingester.parseBlocks( new File("src/test/resources/recommendations1.yaml") );
        assertThat( contents, is("ok"));
    }

    @Module( includes=DaggerModule.class, overrides=true, injects=MyrrixPrefsIngestTest.class)
    static class TestModule {

        @Provides
        @Singleton
        EsItemFactory provideItemFactory() {
        	return MockFactories.inMemoryItemFactory();
        }
    }
}
