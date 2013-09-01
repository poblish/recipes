/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.test.TestDataUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSearchServiceTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private ObjectMapper mapper = GRAPH.get( ObjectMapper.class );
	private IItemPersistence items = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipes = GRAPH.get( EsRecipeFactory.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private ISearchAPI searchService = GRAPH.get( EsSearchService.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		items.deleteAll();
		recipes.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException, InterruptedException {
		GRAPH.get( ItemsLoader.class ).load();

		dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");
		dataUtils.parseIngredientsFrom("bol1.txt");
		dataUtils.parseIngredientsFrom("bol2.txt");

		Thread.sleep(900);
	}

	@Test
	public void partialTestForItem1() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("ging", 5);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Ginger}, ItemSearchResult{itemName=Ginger Paste}, ItemSearchResult{itemName=piece of fresh ginger}, ItemSearchResult{itemName=Aubergine}, ItemSearchResult{itemName=Linguine}]"));
		assertThat( results1.size(), is(5));
	}

	@Test
	public void partialTestForItemWithSize2() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("cori", 5);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Coriander}, ItemSearchResult{itemName=Coriander Powder}, ItemSearchResult{itemName=Coriander Seeds}, ItemSearchResult{itemName=Chorizo}, ItemSearchResult{itemName=Cornflour}]"));
		assertThat( results1.size(), is(5));
	}

	@Test
	public void partialTestForAlias() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("cilant", 1);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
		assertThat( results1.size(), is(1));
	}

	@Test
	public void partialTestForAlias2() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("antro", 1);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
		assertThat( results1.size(), is(1));
	}

	@Test
	public void partialTestForAlias3() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("Pobla", 1);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Ancho Chile}]"));
		assertThat( results1.size(), is(1));
	}

	@Test
	public void partialTestForAlias4() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("Bulb", 5);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Fennel Bulb}, ItemSearchResult{itemName=Bulgar Wheat}]"));
	}

	@Test
	public void partialTestForMissingItem() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("qux");
		assertThat( results1.size(), is(0));
	}

	@Test
	public void partialTestForRecipe1() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("curr", 4);
		assertThat( results1.toString(), is("[RecipeSearchResult{recipeName=chCashBlackSpiceCurry.txt}, ItemSearchResult{itemName=Curry Leaves}, ItemSearchResult{itemName=Curry Powder}, ItemSearchResult{itemName=Curry Paste}]"));
		assertThat( results1.size(), is(4));
	}

	@Test
	public void testSerializeResults() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("curr", 4);
		final String stringOutput = mapper.writeValueAsString(results1);
		assertThat( stringOutput, containsString("\"name\":\"Curry Leaves\""));
		assertThat( stringOutput, containsString("\"title\":\"chCashBlackSpiceCurry.txt\""));
		assertThat( stringOutput, containsString("\"type\":\"item\""));
		assertThat( stringOutput, containsString("\"type\":\"recipe\""));
		assertThat( /* Produce JsonNode */ mapper.valueToTree(results1).toString(), is(stringOutput));
	}
}
