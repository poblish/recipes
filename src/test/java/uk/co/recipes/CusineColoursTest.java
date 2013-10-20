/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CusineColoursTest {

    @Inject ObjectMapper mapper;

	@BeforeClass
    public void injectDependencies() {
        ObjectGraph.create( new TestModule() ).inject(this);
    }

	@Test
	void testParse() throws IOException {
		final Map<String,String> colsMap = mapper.readValue( new File("src/test/resources/cuisineColours.json"), mapper.getTypeFactory().constructMapType( HashMap.class, String.class, String.class));
		assertThat( colsMap.size(), greaterThan(42));
		assertThat( colsMap.containsKey("British"), is(true));
		// System.out.println(colsMap);
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=CusineColoursTest.class)
    static class TestModule {}
}
