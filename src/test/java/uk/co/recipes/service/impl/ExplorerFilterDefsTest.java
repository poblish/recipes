/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.tags.FlavourTags.CITRUS;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.service.api.IExplorerFilterDef;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.MeatAndFishTags;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ExplorerFilterDefsTest {

    @Inject ObjectMapper mapper;

	private ExplorerFilterDefs filterDefs = new ExplorerFilterDefs();

	@BeforeClass
	public void injectDependencies() throws ClientProtocolException, IOException {
        ObjectGraph.create( new TestModule() ).inject(this);
	}

	@Test
	public void testSer() throws IOException {
        final IExplorerFilterDef fDef1 = filterDefs.build().includeTags( CommonTags.FRUIT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.writeValueAsString(fDef1), is("{\"includes\":[{\"filter\":\"Tag|FRUIT\"},{\"filter\":\"Tag|VEGETABLE\"}]}"));

        final IExplorerFilterDef fDef2 = filterDefs.build().excludeTags( MeatAndFishTags.MEAT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.writeValueAsString(fDef2), is("{\"excludes\":[{\"filter\":\"Tag|MEAT\"},{\"filter\":\"Tag|VEGETABLE\"}]}"));

        final IExplorerFilterDef fDef3 = filterDefs.build().includeTags( CommonTags.FRUIT, CITRUS ).excludeTags( MeatAndFishTags.MEAT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.writeValueAsString(fDef3), is("{\"includes\":[{\"filter\":\"Tag|FRUIT\"},{\"filter\":\"Tag|CITRUS\"}],\"excludes\":[{\"filter\":\"Tag|MEAT\"},{\"filter\":\"Tag|VEGETABLE\"}]}"));
	}

    @Test
    public void testSerWithValues() throws IOException {
        final IExplorerFilterDef fDef1 = filterDefs.build().includeTag( CommonTags.FRUIT, "yes").includeTag( CommonTags.VEGETABLE, "maybe").excludeTag( CITRUS, "no").toFilterDef();
        assertThat( mapper.writeValueAsString(fDef1), is("{\"includes\":[{\"filter\":\"Tag|FRUIT|yes\"},{\"filter\":\"Tag|VEGETABLE|maybe\"}],\"excludes\":[{\"filter\":\"Tag|CITRUS|no\"}]}"));
    }

	@Test
	public void testDeser() throws IOException {
        final IExplorerFilterDef fDef1 = filterDefs.build().includeTags( CITRUS, CommonTags.FRUIT ).excludeTags( MeatAndFishTags.SAUSAGE, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.readValue( "{\"includes\":[{\"filter\":\"Tag|CITRUS\"},{\"filter\":\"Tag|FRUIT\"}],\"excludes\":[{\"filter\":\"Tag|SAUSAGE\"},{\"filter\":\"Tag|VEGETABLE\"}]}", DefaultExplorerFilterDef.class), is(fDef1));
	}

    @Test
    public void testDeserWithValues() throws IOException {
        final IExplorerFilterDef fDef1 = filterDefs.build().includeTag( CommonTags.FRUIT, "yes").includeTag( CommonTags.VEGETABLE, "maybe").excludeTag( CITRUS, "no").toFilterDef();
        assertThat( mapper.readValue("{\"includes\":[{\"filter\":\"Tag|FRUIT|yes\"},{\"filter\":\"Tag|VEGETABLE|maybe\"}],\"excludes\":[{\"filter\":\"Tag|CITRUS|no\"}]}", DefaultExplorerFilterDef.class), is(fDef1));
    }

    @Module( includes=DaggerModule.class, overrides=true, injects=ExplorerFilterDefsTest.class)
    static class TestModule {}
}
