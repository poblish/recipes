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
	}

	@Test
	public void testIncludeTags() throws IOException {
		final IExplorerFilter filter = filters.includeTags( CommonTags.FRUIT );
		assertThat( filter.idsToInclude().length, is(24));
		assertThat( filter.idsToExclude().length, is(0));
	}

	@Test
	public void testExcludeTags() throws IOException {
		final IExplorerFilter filter = filters.excludeTags( CommonTags.FRUIT );
		assertThat( filter.idsToInclude().length, is(0));
		assertThat( filter.idsToExclude().length, is(24));
	}
}