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
        assertThat( mapper.writeValueAsString(fDef1), is("{\"includeTags\":[\"FRUIT\",\"VEGETABLE\"]}"));

        final IExplorerFilterDef fDef2 = filterDefs.build().excludeTags( MeatAndFishTags.MEAT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.writeValueAsString(fDef2), is("{\"excludeTags\":[\"MEAT\",\"VEGETABLE\"]}"));

        final IExplorerFilterDef fDef3 = filterDefs.build().includeTags( CommonTags.FRUIT, CITRUS ).excludeTags( MeatAndFishTags.MEAT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.writeValueAsString(fDef3), is("{\"includeTags\":[\"FRUIT\",\"CITRUS\"],\"excludeTags\":[\"MEAT\",\"VEGETABLE\"]}"));
	}

	@Test
	public void testDeser() throws IOException {
        final IExplorerFilterDef fDef1 = filterDefs.build().includeTags( CommonTags.FRUIT, CITRUS ).excludeTags( MeatAndFishTags.MEAT, CommonTags.VEGETABLE ).toFilterDef();
        assertThat( mapper.readValue( "{\"includeTags\":[\"FRUIT\",\"CITRUS\"],\"excludeTags\":[\"MEAT\",\"VEGETABLE\"]}", DefaultExplorerFilterDef.class), is(fDef1));
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=ExplorerFilterDefsTest.class)
    static class TestModule {}
}