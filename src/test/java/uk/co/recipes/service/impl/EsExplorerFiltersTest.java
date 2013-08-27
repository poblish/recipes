/**
 * 
 */
package uk.co.recipes.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.tags.CommonTags;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsExplorerFiltersTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private IItemPersistence users = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipes = GRAPH.get( EsRecipeFactory.class );
//	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private EsExplorerFilters filters = GRAPH.get( EsExplorerFilters.class );
//	private IExplorerAPI explorer = GRAPH.get( MyrrixExplorerService.class );


	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		users.deleteAll();
		recipes.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsAndOneRecipe() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();
//		dataUtils.parseIngredientsFrom("bulk.txt");

		Thread.sleep(900);  // FIXME Ensure everything's in ES index
	}

    @Test
    public void testEmptyFilter() throws IOException {
        final IExplorerFilter filter = EsExplorerFilters.nullFilter();
        assertThat( filter.idsToInclude().length, is(0));
        assertThat( filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeTags() throws IOException {
        final IExplorerFilter filter = filters.build().includeTags( CommonTags.FRUIT ).toFilter();
        assertThat( filter.idsToInclude().length, is(24));
        assertThat( filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeMultipleTags() throws IOException {
        final IExplorerFilter filter = filters.build().includeTags( CommonTags.FRUIT, CommonTags.CITRUS ).toFilter();
        assertThat( filter.idsToInclude().length, is(11));
        assertThat( filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeMultipleTags2() throws IOException {
        final IExplorerFilter filter = filters.build().includeTags( CommonTags.FRUIT, CommonTags.CITRUS, CommonTags.MEAT ).toFilter();
        assertThat( filter.idsToInclude().length, is(0));
        assertThat( filter.idsToExclude().length, is(0));
    }

    @Test
    public void testIncludeMultipleInclAndExclTags() throws IOException {
        final IExplorerFilter filter = filters.build().includeTags( CommonTags.FRUIT, CommonTags.CITRUS ).excludeTags( CommonTags.MEAT, CommonTags.VEGETABLE ).toFilter();
        assertThat( filter.idsToInclude().length, is(11));
        assertThat( filter.idsToExclude().length, is(82));
    }

    @Test
    public void testIncludeMultipleTagsReordered() throws IOException {
        final IExplorerFilter filter = filters.build().includeTags( CommonTags.CITRUS, CommonTags.FRUIT ).toFilter();
        assertThat( filter.idsToInclude().length, is(11));
        assertThat( filter.idsToExclude().length, is(0));
    }

    @Test
    public void testExcludeTags() throws IOException {
        final IExplorerFilter filter = filters.build().excludeTags( CommonTags.FRUIT ).toFilter();
        assertThat( filter.idsToInclude().length, is(0));
        assertThat( filter.idsToExclude().length, is(24));
    }

    @Test
    public void testExcludeMultipleTags() throws IOException {
        final IExplorerFilter filter = filters.build().excludeTags( CommonTags.FRUIT, CommonTags.MEAT ).toFilter();
        assertThat( filter.idsToInclude().length, is(0));
        assertThat( filter.idsToExclude().length, is(56));
    }
}