/**
 *
 */
package uk.co.recipes.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Component;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.co.recipes.DomainTestUtils.names;
import static uk.co.recipes.service.api.ESearchArea.ITEMS;
import static uk.co.recipes.service.api.ESearchArea.RECIPES;


/**
 * TODO
 *
 * @author andrewregan
 */
public class EsSearchServiceTest {

    @Inject
    EsItemFactory items;
    @Inject
    EsRecipeFactory recipes;
    @Inject
    EsUserFactory userFactory;

    @Inject
    ItemsLoader loader;
    @Inject
    ObjectMapper mapper;
    @Inject
    TestDataUtils dataUtils;
    @Inject
    EsSearchService searchService;

    @BeforeClass
    public void cleanIndices() throws IOException {
        DaggerEsSearchServiceTest_TestComponent.create().inject(this);

        items.deleteAll();
        recipes.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws IOException, InterruptedException {
        loader.load();

        final IUser adminUser = userFactory.adminUser();

        dataUtils.parseIngredientsFrom(adminUser, "chCashBlackSpiceCurry.txt");
        dataUtils.parseIngredientsFrom(adminUser, "bol1.txt");
        dataUtils.parseIngredientsFrom(adminUser, "bol2.txt");

        Thread.sleep(900);
    }

    // Will find > 1, because it'll find Turmeric as a *constituent* of Curry Powder, etc.
    @Test
    public void findItemByName1Word() throws IOException {
        final List<ICanonicalItem> results1 = searchService.findItemsByName("turmeric");
        assertThat(results1.size(), greaterThan(1));
        assertThat(searchService.countItemsByName("turmeric"), greaterThan(1));
        assertThat(results1.iterator().next().getCanonicalName(), is("Turmeric"));
    }

    @Test
    public void findItemByName2Words() throws IOException {
        final List<ICanonicalItem> results1 = searchService.findItemsByName("Grand Marnier");
        assertThat(results1.size(), is(1));
        assertThat(searchService.countItemsByName("Grand Marnier"), is(1));
    }

    @Test
    public void findRecipeByName1Word() throws IOException {
        final List<IRecipe> results1 = searchService.findRecipesByName("chicken");
        assertThat(results1.size(), is(2));
        assertThat(searchService.countRecipesByName("chicken"), is(2));
    }

    @Test
    public void findRecipeByName2Words() throws IOException {
        final List<IRecipe> results1 = searchService.findRecipesByName("minced beef");
        assertThat(results1.size(), is(2));
        assertThat(searchService.countRecipesByName("minced beef"), is(2));
    }

    @Test
    public void partialTestForItem1() throws IOException {
        final int numToFind = 8;
        final List<String> names = names(searchService.findPartial("ging", numToFind, ITEMS));
        assertThat(names, containsInAnyOrder("Ginger", "Ginger Puree", "Ginger Wine", "Crystallised Ginger", "Root Ginger", "Stem Ginger", "Gingernut biscuits", "Galangal"));
        assertThat(names.size(), is(numToFind));
    }

    @Test
    public void partialTestForItemWithSize2() throws IOException {
        final int numToFind = 6;
        final List<String> names = names(searchService.findPartial("cori", numToFind, ITEMS));
        assertThat(names, containsInAnyOrder("Pecorino", "Coriander Powder", "Coriander", "Coriander Seeds", "Cajun Seasoning", "Chorizo"));
        assertThat(names.size(), is(numToFind));
    }

    @Test
    public void partialTestForAlias() throws IOException {
        final int numToFind = 1;
        final List<ISearchResult<?>> results1 = searchService.findPartial("cilant", numToFind, ITEMS);
        assertThat(results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
        assertThat(results1.size(), is(numToFind));
    }

    @Test
    public void partialTestForAlias2() throws IOException {
        final int numToFind = 1;
        final List<ISearchResult<?>> results1 = searchService.findPartial("antro", numToFind, ITEMS);
        assertThat(results1.toString(), is("[ItemSearchResult{itemName=Coriander}]"));
        assertThat(results1.size(), is(numToFind));
    }

    @Test
    public void partialTestForAlias3() throws IOException {
        final int numToFind = 1;
        final List<ISearchResult<?>> results1 = searchService.findPartial("Pobla", numToFind, ITEMS);
        assertThat(results1.toString(), is("[ItemSearchResult{itemName=Ancho Chile}]"));
        assertThat(results1.size(), is(numToFind));
    }

    @Test
    public void partialTestForAlias4() throws IOException {
        final List<String> names = names(searchService.findPartial("Bulb", 5, ITEMS));
        assertThat(names, containsInAnyOrder("Garlic Bulb", "Fennel Bulb", "Bulgur Wheat"));
    }

    @Test
    public void partialTestForMissingItem() throws IOException {
        final List<ISearchResult<?>> results1 = searchService.findPartial("qux", ITEMS);
        assertThat(results1.size(), is(0));
    }

    // AGR Pretty yucky, this
    @Test
    public void partialTestForRecipe1() throws IOException {
        final String output = names(searchService.findPartial("curr", 12, ITEMS, RECIPES)).toString();
        assertThat(output, allOf(containsString("Curry Paste"), containsString("Blackcurrants"), containsString("Curry Leaves")));
    }

    @Test
    public void testSerializeResults() throws IOException {
        final List<ISearchResult<?>> results1 = searchService.findPartial("curr", 4, ITEMS, RECIPES);
        final String stringOutput = mapper.writeValueAsString(results1);
        assertThat(stringOutput, containsString("\"id\":"));  // at least one Id
        assertThat(stringOutput, containsString("\"displayName\":\"Redcurrants\""));
        assertThat(stringOutput, containsString("\"displayName\":\"chCashBlackSpiceCurry.txt\""));
        assertThat(stringOutput, containsString("\"type\":\"item\""));
        assertThat(stringOutput, containsString("\"type\":\"recipe\""));
        assertThat( /* Produce JsonNode */ mapper.valueToTree(results1).toString(), is(stringOutput));
    }

    @Singleton
    @Component(modules = {DaggerModule.class})
    public interface TestComponent {
        void inject(final EsSearchServiceTest runner);
    }
}