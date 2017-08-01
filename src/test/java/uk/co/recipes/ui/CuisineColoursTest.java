/**
 * 
 */
package uk.co.recipes.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;

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
		DaggerCuisineColoursTest_TestComponent.create().inject(this);
    }

	@Test
	void testParse() throws IOException {
		assertThat( colours.getMap().size(), greaterThan(42));
		assertThat( colours.getMap().containsKey("British"), is(true));
		// System.out.println(colsMap);
	}

	@Singleton
	@Component(modules={ DaggerModule.class })
	public interface TestComponent {
		void inject(final CuisineColoursTest runner);
	}
}
