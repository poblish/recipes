/**
 * 
 */
package uk.co.recipes.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.inject.Inject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.ui.CuisineColours;
import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CuisineColoursTest {

    @Inject CuisineColours colours;

	@BeforeClass
    public void injectDependencies() {
        ObjectGraph.create( new TestModule() ).inject(this);
    }

	@Test
	void testParse() throws IOException {
		assertThat( colours.getMap().size(), greaterThan(42));
		assertThat( colours.getMap().containsKey("British"), is(true));
		// System.out.println(colsMap);
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=CuisineColoursTest.class)
    static class TestModule {}
}
