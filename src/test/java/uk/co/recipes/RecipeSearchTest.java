/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.ExplorerFilterDefs;
import uk.co.recipes.tags.MeatAndFishTags;
import uk.co.recipes.test.TestDataUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeSearchTest {

	@Inject EsItemFactory itemFactory;
	@Inject EsRecipeFactory recipeFactory;
	@Inject EsUserFactory userFactory;

	@Inject Client esClient;
	@Inject ItemsLoader loader;
	@Inject TestDataUtils dataUtils;
	@Inject EsSearchService searchApi;
	@Inject EsExplorerFilters explorerFilters;

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
        ObjectGraph.create( new TestModule() ).inject(this);

        itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		loader.load();

		final IUser adminUser = userFactory.adminUser();

		dataUtils.parseIngredientsFrom( adminUser, "inputs3.txt");
		dataUtils.parseIngredientsFrom( adminUser, "chCashBlackSpiceCurry.txt");
		dataUtils.parseIngredientsFrom( adminUser, "bol1.txt");
		dataUtils.parseIngredientsFrom( adminUser, "bol2.txt");
		dataUtils.parseIngredientsFrom( adminUser, "chineseBeef.txt");

        while ( recipeFactory.countAll() < 5) {
        	Thread.sleep(200); // Wait for saves to appear...
        }
	}

	@Test
	public void findGarlicRecipes() throws InterruptedException, IOException {
		assertThat( searchApi.countRecipesByName("garlic"), is(4));
	}

//	@Test
//	public void testUnusedTag() throws IOException {
//		assertThat( searchApi.findRecipesByTag( NationalCuisineTags.GERMAN ).size(), is(0));
//	}

	@Test
	public void testSearchByTagWithSorting() throws IOException {
		final List<ICanonicalItem> matches = searchApi.findItemsByTag( MeatAndFishTags.MEAT );

		final List<String> returnedNames = FluentIterable.from(matches).transform( new Function<ICanonicalItem, String>() {
			public String apply( ICanonicalItem input) {
				return input.getCanonicalName();
			}
		}).toList();

		assertThat( returnedNames, is( Ordering.from( String.CASE_INSENSITIVE_ORDER ).sortedCopy(returnedNames) ));
	}

	@Test
	public void testSearchByTagAndFilters() throws IOException {
		final int numItems = searchApi.findItemsByTag( MeatAndFishTags.MEAT ).size();

        final long[] foundRecipeIds = searchApi.findRecipeIdsByTag( MeatAndFishTags.MEAT );
        assertThat( foundRecipeIds.length, greaterThanOrEqualTo(4));  // Surely 5 ?!?

		final IExplorerFilter filter = explorerFilters.from( new ExplorerFilterDefs().build().includeTags( MeatAndFishTags.MEAT ).toFilterDef() );
		assertThat( filter.idsToInclude().length, is( foundRecipeIds.length + numItems));
		assertThat( filter.idsToExclude().length, is(0));

		final IExplorerFilter filter2 = explorerFilters.from( new ExplorerFilterDefs().build().excludeTags( MeatAndFishTags.MEAT ).toFilterDef() );
		assertThat( filter2.idsToInclude().length, is(0));
		assertThat( filter2.idsToExclude().length, is( foundRecipeIds.length + numItems));
	}

	@AfterClass
	public void shutDown() {
		esClient.close();
	}

	// Used by main method, not by CurryFrenzyLoader itself!
    @Module( includes=DaggerModule.class, overrides=true, injects=RecipeSearchTest.class)
    static class TestModule {}
}