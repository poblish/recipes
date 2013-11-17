/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.co.recipes.service.api.ESearchArea.ITEMS;
import static uk.co.recipes.service.api.ESearchArea.RECIPES;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.test.TestDataUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSearchServiceTest {

	@Inject EsItemFactory items;
	@Inject EsRecipeFactory recipes;
	@Inject EsUserFactory userFactory;

	@Inject ItemsLoader loader;
	@Inject ObjectMapper mapper;
	@Inject TestDataUtils dataUtils;
	@Inject EsSearchService searchService;

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
        ObjectGraph.create( new TestModule() ).inject(this);

		items.deleteAll();
		recipes.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException, InterruptedException {
		loader.load();

		final IUser adminUser = userFactory.adminUser();

		dataUtils.parseIngredientsFrom( adminUser, "chCashBlackSpiceCurry.txt");
		dataUtils.parseIngredientsFrom( adminUser, "bol1.txt");
		dataUtils.parseIngredientsFrom( adminUser, "bol2.txt");

		Thread.sleep(900);
	}

	// Will find > 1, because it'll find Turmeric as a *constituent* of Curry Powder, etc.
	@Test
	public void findItemByName1Word() throws IOException {
		final List<ICanonicalItem> results1 = searchService.findItemsByName("turmeric");
		assertThat( results1.size(), greaterThan(1));
		assertThat( searchService.countItemsByName("turmeric"), greaterThan(1));
		assertThat( results1.iterator().next().getCanonicalName(), is("Turmeric"));
	}

	@Test
	public void findItemByName2Words() throws IOException {
		final List<ICanonicalItem> results1 = searchService.findItemsByName("Grand Marnier");
		assertThat( results1.size(), is(1));
		assertThat( searchService.countItemsByName("Grand Marnier"), is(1));
	}

	@Test
	public void findRecipeByName1Word() throws IOException {
		final List<IRecipe> results1 = searchService.findRecipesByName("chicken");
		assertThat( results1.size(), is(2));
		assertThat( searchService.countRecipesByName("chicken"), is(2));
	}

	@Test
	public void findRecipeByName2Words() throws IOException {
		final List<IRecipe> results1 = searchService.findRecipesByName("minced beef");
		assertThat( results1.size(), is(2));
		assertThat( searchService.countRecipesByName("minced beef"), is(2));
	}

	@Test
	public void partialTestForItem1() throws IOException {
		final int numToFind = 8;
		final List<ISearchResult<?>> results1 = searchService.findPartial("ging", numToFind, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Ginger}, ItemSearchResult{itemName=Ginger Puree}, ItemSearchResult{itemName=Crystallised Ginger}, ItemSearchResult{itemName=Root Ginger}, ItemSearchResult{itemName=Stem Ginger}, ItemSearchResult{itemName=Gingernut biscuits}, ItemSearchResult{itemName=Ginger Wine}, ItemSearchResult{itemName=Galangal}]"));
		assertThat( results1.size(), is(numToFind));
	}

	@Test
	public void partialTestForItemWithSize2() throws IOException {
		final int numToFind = 6;
		final List<ISearchResult<?>> results1 = searchService.findPartial("cori", numToFind, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Pecorino}, ItemSearchResult{itemName=Coriander Powder}, ItemSearchResult{itemName=Coriander}, ItemSearchResult{itemName=Coriander Seeds}, ItemSearchResult{itemName=Cajun Seasoning}, ItemSearchResult{itemName=Chorizo}]"));
		assertThat( results1.size(), is(numToFind));
	}

	@Test
	public void partialTestForAlias() throws IOException {
		final int numToFind = 1;
		final List<ISearchResult<?>> results1 = searchService.findPartial("cilant", numToFind, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
		assertThat( results1.size(), is(numToFind));
	}

	@Test
	public void partialTestForAlias2() throws IOException {
		final int numToFind = 1;
		final List<ISearchResult<?>> results1 = searchService.findPartial("antro", numToFind, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
		assertThat( results1.size(), is(numToFind));
	}

	@Test
	public void partialTestForAlias3() throws IOException {
		final int numToFind = 1;
		final List<ISearchResult<?>> results1 = searchService.findPartial("Pobla", numToFind, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Ancho Chile}]"));
		assertThat( results1.size(), is(numToFind));
	}

	@Test
	public void partialTestForAlias4() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("Bulb", 5, ITEMS);
		assertThat( results1.toString(), is("[ItemSearchResult{itemName=Garlic Bulb}, ItemSearchResult{itemName=Fennel Bulb}, ItemSearchResult{itemName=Bulgur Wheat}]"));
	}

	@Test
	public void partialTestForMissingItem() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("qux", ITEMS);
		assertThat( results1.size(), is(0));
	}

	@Test
	public void partialTestForRecipe1() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("curr", 5, ITEMS, RECIPES);
		assertThat( results1.toString(), is("[RecipeSearchResult{recipeName=chCashBlackSpiceCurry.txt}, ItemSearchResult{itemName=Blackcurrants}, ItemSearchResult{itemName=Redcurrants}, ItemSearchResult{itemName=Currant}, ItemSearchResult{itemName=Curry Leaves}]"));
		assertThat( results1.size(), is(5));
	}

	@Test
	public void testSerializeResults() throws IOException {
		final List<ISearchResult<?>> results1 = searchService.findPartial("curr", 4, ITEMS, RECIPES);
		final String stringOutput = mapper.writeValueAsString(results1);
		assertThat( stringOutput, containsString("\"id\":"));  // at least one Id
		assertThat( stringOutput, containsString("\"displayName\":\"Redcurrants\""));
		assertThat( stringOutput, containsString("\"displayName\":\"chCashBlackSpiceCurry.txt\""));
		assertThat( stringOutput, containsString("\"type\":\"item\""));
		assertThat( stringOutput, containsString("\"type\":\"recipe\""));
		assertThat( /* Produce JsonNode */ mapper.valueToTree(results1).toString(), is(stringOutput));
	}

    @Module( includes=DaggerModule.class, overrides=true, injects=EsSearchServiceTest.class)
    static class TestModule {}
}