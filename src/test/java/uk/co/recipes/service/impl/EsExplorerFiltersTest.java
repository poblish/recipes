/**
 *
 */
package uk.co.recipes.service.impl;

import dagger.Component;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.MeatAndFishTags;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.tags.FlavourTags.CITRUS;

/**
 * TODO
 *
 * @author andrewregan
 */
public class EsExplorerFiltersTest {

    @Inject
    EsItemFactory itemFactory;
    @Inject
    EsUserFactory users;
    @Inject
    EsRecipeFactory recipes;
    @Inject
    ItemsLoader loader;
    @Inject
    EsExplorerFilters filters;
    private ExplorerFilterDefs filterDefs = new ExplorerFilterDefs();

    @BeforeClass
    public void cleanIndices() throws IOException {
        DaggerEsExplorerFiltersTest_TestComponent.create().inject(this);

        users.deleteAll();
        recipes.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsAndOneRecipe() throws InterruptedException, IOException {
        loader.load();
//		dataUtils.parseIngredientsFrom("bulk.txt");

        // Thread.sleep(900);  // FIXME Ensure everything's in ES index
    }

    @Test
    public void testEmptyFilter() throws IOException {
        final IExplorerFilter filter = EsExplorerFilters.nullFilter();
        assertThat(filter.idsToInclude().length, is(0));
        assertThat(filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeTags() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CommonTags.FRUIT).toFilterDef());
        assertThat(filter.idsToInclude().length, greaterThan(27));
        assertThat(filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeMultipleTags() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CommonTags.FRUIT, CITRUS).toFilterDef());
        assertThat(filter.idsToInclude().length, greaterThan(12));
        assertThat(filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeMultipleTags2_Order1() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CommonTags.FRUIT, CITRUS, MeatAndFishTags.MEAT).toFilterDef());
        assertThat(filter.idsToInclude().length, is(1));
        assertThat(filter.idsToExclude().length, is(1));
        assertThat(filter.idsToInclude()[0], is(-1L));    // i.e. unusable!
        assertThat(filter.idsToExclude()[0], is(-1L));    // i.e. unusable!
    }

    @Test
    public void testIncludeMultipleTags2_Order2() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CITRUS, CommonTags.FRUIT, MeatAndFishTags.MEAT).toFilterDef());
        assertThat(filter.idsToInclude().length, is(1));
        assertThat(filter.idsToExclude().length, is(1));
        assertThat(filter.idsToInclude()[0], is(-1L));    // i.e. unusable!
        assertThat(filter.idsToExclude()[0], is(-1L));    // i.e. unusable!
    }

    @Test
    public void testIncludeMultipleTags2_Order3() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(MeatAndFishTags.MEAT, CommonTags.FRUIT, CITRUS).toFilterDef());
        assertThat(filter.idsToInclude().length, is(1));
        assertThat(filter.idsToExclude().length, is(1));
        assertThat(filter.idsToInclude()[0], is(-1L));    // i.e. unusable!
        assertThat(filter.idsToExclude()[0], is(-1L));    // i.e. unusable!
    }

    @Test
    public void testIncludeMultipleInclAndExclTags() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CommonTags.FRUIT, CITRUS).excludeTags(MeatAndFishTags.MEAT, CommonTags.VEGETABLE).toFilterDef());
        assertThat(filter.idsToInclude().length, greaterThan(12));
        assertThat(filter.idsToExclude().length, greaterThan(84));
    }

    @Test
    public void testIncludeMultipleTagsReordered() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().includeTags(CITRUS, CommonTags.FRUIT).toFilterDef());
        assertThat(filter.idsToInclude().length, greaterThan(12));
        assertThat(filter.idsToExclude().length, is(0));
    }

    @Test
    public void testExcludeTags() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().excludeTags(CommonTags.FRUIT).toFilterDef());
        assertThat(filter.idsToInclude().length, is(0));
        assertThat(filter.idsToExclude().length, greaterThan(27));
    }

    @Test
    public void testExcludeMultipleTags() throws IOException {
        final IExplorerFilter filter = filters.from(filterDefs.build().excludeTags(CommonTags.FRUIT, MeatAndFishTags.MEAT).toFilterDef());
        assertThat(filter.idsToInclude().length, is(0));
        assertThat(filter.idsToExclude().length, greaterThan(60));
    }

    @Singleton
    @Component(modules = {DaggerModule.class})
    public interface TestComponent {
        void inject(final EsExplorerFiltersTest runner);
    }
}