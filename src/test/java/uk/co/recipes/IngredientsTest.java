package uk.co.recipes;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.Units;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.ExplorerFilterDefs;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.tags.FlavourTags;
import uk.co.recipes.tags.MeatAndFishTags;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;


/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class IngredientsTest {

    @Inject EsItemFactory itemFactory;
    @Inject EsUserFactory userFactory;
    @Inject EsRecipeFactory recipes;
    @Inject EsSequenceFactory sequenceFactory;
    @Inject ItemsLoader loader;

    @Inject MyrrixUpdater updater;

    @Inject EsSearchService searchService;
    @Inject MyrrixExplorerService explorerApi;
    @Inject EsExplorerFilters explorerFilters;
    @Inject MyrrixRecommendationService recsApi;

    @Inject IEventService events;
//  @Inject IIngredientQuantityScoreBooster booster;

    private void injectDependencies() {
		DaggerIngredientsTest_TestComponent.create().inject(this);
    }

	@BeforeClass
	public void cleanIndices() throws IOException {
	    injectDependencies();

		updater.startListening();

		itemFactory.deleteAll();
		recipes.deleteAll();

        sequenceFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException {
	    loader.load();
	}

	@Test
	public void inheritedBaseAmountsTest() throws IOException {
		final Optional<ICanonicalItem> item = itemFactory.get("Rye Flour");
		assertThat( item.get().getBaseAmount(), notNullValue());
	}

    @Test
    public void inheritedConstituentsTest() throws IOException {
        final ICanonicalItem const1 = itemFactory.get("Maize").get();

        // FIXME: Should test _merging_ from parent too
        assertThat( itemFactory.get("Maize Porridge").get().getConstituents(), hasItem(const1));  // Yes, we know this one will
        assertThat( itemFactory.get("Polenta").get().getConstituents(), hasItem(const1));         // But is it inherited?
        assertThat( itemFactory.get("Orange").get().getConstituents(), not( hasItem(const1) ));   // Sanity check
    }

	@Test
	public void accentsTest() throws IOException {
		assertThat( itemFactory.get("Lovely Glacé Cherries").isPresent(), is(false));

		final ICanonicalItem newItem = new CanonicalItem("Lovely Glacé Cherries");
		itemFactory.put( newItem, itemFactory.toStringId(newItem));

		final Optional<ICanonicalItem> loaded = itemFactory.get("Lovely Glacé Cherries");
		assertThat( loaded.get(), is(newItem));
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException, InterruptedException {
		final ICanonicalItem lamb = itemFactory.getOrCreate( "Lamb", () -> {
            final ICanonicalItem meat = new CanonicalItem("Lamb");
            meat.addTag( MeatAndFishTags.MEAT );
            meat.addTag( MeatAndFishTags.RED_MEAT );
            return meat;
        });

		final ICanonicalItem lambNeck = itemFactory.getOrCreate( "Lamb Neck", () -> new CanonicalItem("Lamb Neck", Optional.of(lamb)));

		final Ingredient lambIngredient = new Ingredient( lambNeck, new Quantity( Units.GRAMMES, 900));
		lambIngredient.addNote( ENGLISH, "Neck fillets, cut into large chunks");

		///////////////////////////////////////////////////

		final ICanonicalItem bacon = itemFactory.getOrCreate( "Bacon", () -> {
            final ICanonicalItem meat = new CanonicalItem("Bacon");
            meat.addTag( MeatAndFishTags.MEAT );
            return meat;
        });

		final ICanonicalItem ssBacon = itemFactory.getOrCreate( "Lamb Neck", () -> new CanonicalItem("Smoked Streaky Bacon", Optional.of(bacon)));

		final Ingredient baconIngredient = new Ingredient( ssBacon, new Quantity( Units.GRAMMES, 200));
		baconIngredient.addNote( ENGLISH, "Preferably in one piece, skinned and cut into pieces");

		///////////////////////////////////////////////////

		itemFactory.waitUntilRefreshed();

		final List<ICanonicalItem> results = searchService.findItemsByTag( MeatAndFishTags.MEAT );
		assertThat( results.size(), greaterThanOrEqualTo(22));
		assertThat( results, hasItem( itemFactory.getByName("beef_stock") ));
		assertThat( results, hasItem( itemFactory.getByName("lamb") ));
		assertThat( results, hasItem( itemFactory.getByName("diced_chicken") ));

		final IExplorerFilter filter = explorerFilters.from( new ExplorerFilterDefs().build().includeTags( MeatAndFishTags.MEAT ).toFilterDef() );
		assertThat( filter.idsToInclude().length, is( results.size() ));
		assertThat( filter.idsToExclude().length, is(0));

		final IExplorerFilter filter2 = explorerFilters.from( new ExplorerFilterDefs().build().excludeTags( MeatAndFishTags.MEAT ).toFilterDef() );
		assertThat( filter2.idsToInclude().length, is(0));
		assertThat( filter2.idsToExclude().length, is( results.size() ));

		///////////////////////////////////////////////////

		final Map<ITag,Serializable> expectedLambTags = Maps.newHashMap();
		expectedLambTags.put( MeatAndFishTags.MEAT, true);
		expectedLambTags.put( MeatAndFishTags.RED_MEAT, true);

		final Map<ITag,Serializable> expectedBaconTags = Maps.newLinkedHashMap();
		expectedBaconTags.put( MeatAndFishTags.MEAT, true);
        expectedBaconTags.put( FlavourTags.SMOKY_SALTY, true);
        expectedBaconTags.put( FlavourTags.UMAMI, true);

		assertThat( lamb.getTags(), is(expectedLambTags));
		assertThat( lambNeck.getTags(), is(expectedLambTags));
		assertThat( bacon.getTags(), is(expectedBaconTags));
		assertThat( lambNeck.parent(), is( Optional.of(lamb) ));
		assertThat( lamb.parent().orNull(), nullValue());
	}

	@Test
	public void testRecommendations() throws IOException, TasteException {
		final IUser user1 = userFactory.getOrCreate( "Andrew Regan", () -> new User( "aregan", "Andrew Regan"));

		assertThat( user1.getId(), greaterThanOrEqualTo(0L));  // Check we've been persisted

		final IUser user2 = userFactory.getOrCreate( "Foo Bar", () -> new User( "foobar", "Foo Bar"));

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

    @Test
    public void testHierarchicalTagPersistence() throws IOException {
        assertThat( loadItem("coriander").getTags().toString(), is("{GRASSY=true, HERB=true}"));
        assertThat( loadItem("coriander_powder").getTags().toString(), is("{GRASSY=true, INDIAN=true, SPICE=true}"));
        assertThat( loadItem("coriander_seeds").getTags().toString(), is("{INDIAN=true, SPICE=true}"));
        assertThat( loadItem("smoked_haddock").getTags().toString(), is("{FISH=true, SEAFOOD=true, SMOKY_SALTY=true}"));

        assertThat( searchService.findItemsByTag( FlavourTags.GRASSY ), hasItem( loadItem("coriander") ));
        assertThat( searchService.findItemsByTag( FlavourTags.GRASSY ), not( hasItem( loadItem("coriander_seeds") )));

        assertThat( searchService.findItemsByTag( FlavourTags.SMOKY_SALTY ), hasItem( loadItem("smoked_haddock") ));
        assertThat( searchService.findItemsByTag( MeatAndFishTags.FISH ), hasItem( loadItem("smoked_haddock") ));
//        assertThat( searchService.findItemsByTag( FlavourTags.SMOKY_SALTY ), not( hasItem( loadItem("smoked_haddock") )));
    }

    private ICanonicalItem loadItem( final String inName) throws IOException {
        return itemFactory.get(inName).get();
    }

	@Singleton
	@Component(modules={ DaggerModule.class })
	public interface TestComponent {
		void inject(final IngredientsTest runner);
	}
}