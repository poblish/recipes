package uk.co.recipes;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.Units;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.tags.MeatAndFishTags;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientsTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IUserPersistence userFactory = GRAPH.get( EsUserFactory.class );
    private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );

    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );

	private ISearchAPI searchService = GRAPH.get( EsSearchService.class );
    private IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );
    private EsExplorerFilters explorerFilters = GRAPH.get( EsExplorerFilters.class );
    private IRecommendationsAPI recsApi = GRAPH.get( MyrrixRecommendationService.class );

    private IEventService events = GRAPH.get( IEventService.class );
//    private IIngredientQuantityScoreBooster booster = GRAPH.get( DefaultIngredientQuantityScoreBooster.class );

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		updater.startListening();

		itemFactory.deleteAll();
		GRAPH.get( EsRecipeFactory.class ).deleteAll();

        sequenceFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException {
		GRAPH.get( ItemsLoader.class ).load();
	}

	@Test
	public void accentsTest() throws IOException {
		assertThat( itemFactory.get("Glacé Cherries").isPresent(), is(false));

		final ICanonicalItem newItem = new CanonicalItem("Glacé Cherries");
		itemFactory.put( newItem, itemFactory.toStringId(newItem));

		final Optional<ICanonicalItem> loaded = itemFactory.get("Glacé Cherries");
		assertThat( loaded.get(), is(newItem));
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException, InterruptedException {
		final ICanonicalItem lamb = itemFactory.getOrCreate( "Lamb", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem meat = new CanonicalItem("Lamb");
				meat.addTag( MeatAndFishTags.MEAT );
				return meat;
			}});

		final ICanonicalItem lambNeck = itemFactory.getOrCreate( "Lamb Neck", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Lamb Neck", Optional.of(lamb));
			}});

		final Ingredient lambIngredient = new Ingredient( lambNeck, new Quantity( Units.GRAMMES, 900));
		lambIngredient.addNote( ENGLISH, "Neck fillets, cut into large chunks");

		///////////////////////////////////////////////////

		final ICanonicalItem bacon = itemFactory.getOrCreate( "Bacon", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem meat = new CanonicalItem("Bacon");
				meat.addTag( MeatAndFishTags.MEAT );
				return meat;
			}});

		final ICanonicalItem ssBacon = itemFactory.getOrCreate( "Lamb Neck", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				return new CanonicalItem("Smoked Streaky Bacon", Optional.of(bacon));
			}});

		final Ingredient baconIngredient = new Ingredient( ssBacon, new Quantity( Units.GRAMMES, 200));
		baconIngredient.addNote( ENGLISH, "Preferably in one piece, skinned and cut into pieces");

		///////////////////////////////////////////////////

		itemFactory.waitUntilRefreshed();

		final List<ICanonicalItem> results = searchService.findItemsByTag( MeatAndFishTags.MEAT );
		assertThat( results.size(), greaterThanOrEqualTo(22));
		assertThat( results, hasItem( itemFactory.getByName("beef_stock") ));
		assertThat( results, hasItem( itemFactory.getByName("lamb") ));
		assertThat( results, hasItem( itemFactory.getByName("diced_chicken") ));

		final IExplorerFilter filter = explorerFilters.build().includeTags( MeatAndFishTags.MEAT ).toFilter();
		assertThat( filter.idsToInclude().length, is( results.size() ));
		assertThat( filter.idsToExclude().length, is(0));

		final IExplorerFilter filter2 = explorerFilters.build().excludeTags( MeatAndFishTags.MEAT ).toFilter();
		assertThat( filter2.idsToInclude().length, is(0));
		assertThat( filter2.idsToExclude().length, is( results.size() ));

		///////////////////////////////////////////////////

		final Map<ITag,Serializable> expectedTags = Maps.newHashMap();
		expectedTags.put( MeatAndFishTags.MEAT, true);

		assertThat( lamb.getTags(), is(expectedTags));
		assertThat( lambNeck.getTags(), is(expectedTags));
		assertThat( bacon.getTags(), is(expectedTags));
		assertThat( lambNeck.parent(), is( Optional.of(lamb) ));
		assertThat( lamb.parent().orNull(), nullValue());
	}

	@Test
	public void testRecommendations() throws IOException, TasteException {
		final IUser user1 = userFactory.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "aregan", "Andrew Regan");
			}
		} );

		assertThat( user1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final IUser user2 = userFactory.getOrCreate( "Foo Bar", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "foobar", "Foo Bar");
			}
		} );

		assertThat( user2.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		events.rateItem( user1, itemFactory.getByName("milk"), (float) Math.random());
		events.rateItem( user1, itemFactory.getByName("red_wine"), (float) Math.random());

		events.rateItem( user2, itemFactory.getByName("ginger"), (float) Math.random());
		events.rateItem( user2, itemFactory.getByName("lemon"), (float) Math.random());
		events.rateItem( user2, itemFactory.getByName("lime"), (float) Math.random());

		final List<ICanonicalItem> recsFor1 = recsApi.recommendIngredients( user1, 20);
		final List<ICanonicalItem> recsFor2 = recsApi.recommendIngredients( user2, 20);

		System.out.println("Recommendations.1: " + recsFor1);
		System.out.println("Recommendations.2: " + recsFor2);

		assertThat( recsFor1.size(), greaterThanOrEqualTo(2));
		assertThat( recsFor2.size(), greaterThanOrEqualTo(2));

//		assertThat( recsFor1, hasItem( recipeFactory.getByName("bulk.txt")  ));
//		assertThat( recsFor1, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));
//
//		assertThat( recsFor2, hasItem( recipeFactory.getByName("bol2.txt")  ));
//		assertThat( recsFor2, hasItem( recipeFactory.getByName("chinesebeef.txt")  ));
//		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("chcashblackspicecurry.txt")  )));
//		assertThat( recsFor2, not(hasItem( recipeFactory.getByName("inputs3.txt")  )));
	}

	@Test
    public void testExplorer() throws IOException, TasteException {
        runSimilarity("Avocado");
        runSimilarity("Lemon");
        runSimilarity("Lime");

        runSimilarity("Broccoli");
        runSimilarity("Green Pepper");
        runSimilarity("Onion");

        runSimilarity("Ginger");
        runSimilarity("Oregano");
        runSimilarity("Paprika");

        runSimilarity("Milk");
        runSimilarity("Coffee");

        runSimilarity("Lamb");
        runSimilarity("Brandy");
        runSimilarity("Red Wine");
        runSimilarity("Fish Sauce");
        runSimilarity("Soy Sauce");
        runSimilarity("Gnocchi");
        runSimilarity("Dark Muscovado Sugar");
        runSimilarity("Cashew Nuts");
        runSimilarity("Semolina");
    }

    private void runSimilarity( final String inName) throws IOException {
        final ICanonicalItem item = itemFactory.get(inName).get();
        assertThat( inName, is( item.getCanonicalName() ));
        System.out.println( Strings.padEnd("Similar to " + inName + ":", 24, ' ') + explorerApi.similarIngredients( item, 10) );
    }
}