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
import uk.co.recipes.api.Units;
import uk.co.recipes.events.api.IEventListener;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.tags.CommonTags;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import dagger.ObjectGraph;

public class IngredientsTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
    private EsSequenceFactory sequenceFactory = GRAPH.get( EsSequenceFactory.class );

    private IEventListener updater = GRAPH.get( MyrrixUpdater.class );

	private ISearchAPI searchService = GRAPH.get( EsSearchService.class );
    private IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );
//    private IRecommendationsAPI recsApi = GRAPH.get( MyrrixRecommendationService.class );
//
//    private IEventService events = GRAPH.get( IEventService.class );

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		updater.startListening();

		itemFactory.deleteAll();
        sequenceFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws IOException {
		GRAPH.get( ItemsLoader.class ).load();
	}

	// See: http://www.bbcgoodfood.com/recipes/5533/herby-lamb-cobbler
	@Test
	public void initialTest() throws IOException, InterruptedException {
		final ICanonicalItem lamb = itemFactory.getOrCreate( "Lamb", new Supplier<ICanonicalItem>() {

			@Override
			public ICanonicalItem get() {
				final ICanonicalItem meat = new CanonicalItem("Lamb");
				meat.addTag( CommonTags.MEAT );
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
				meat.addTag( CommonTags.MEAT );
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

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredient(lambIngredient);
		stage1.addIngredient(baconIngredient);

		final Recipe r = new Recipe("Herby Lamb Cobbler");
		r.addStage(stage1);
		r.addTag( CommonTags.SERVES_COUNT, "4");

		recipeFactory.put( r, recipeFactory.toStringId(r));

		///////////////////////////////////////////////////

		Thread.sleep(1000);  // Time for indexing to happen!

		final List<ICanonicalItem> results = searchService.findItemsByName("MEAT:true");
		assertThat( results.size(), greaterThanOrEqualTo(19));
		assertThat( results, hasItem( itemFactory.getById("beef_stock") ));
		assertThat( results, hasItem( itemFactory.getById("lamb") ));
		assertThat( results, hasItem( itemFactory.getById("diced_chicken") ));

		///////////////////////////////////////////////////

		final Map<ITag,Serializable> expectedTags = Maps.newHashMap();
		expectedTags.put( CommonTags.MEAT, true);

		assertThat( lamb.getTags(), is(expectedTags));
		assertThat( lambNeck.getTags(), is(expectedTags));
		assertThat( bacon.getTags(), is(expectedTags));
		assertThat( lambNeck.parent(), is( Optional.of(lamb) ));
		assertThat( lamb.parent().orNull(), nullValue());
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
    }

    private void runSimilarity( final String inName) throws IOException {
        final ICanonicalItem item = itemFactory.get(inName).get();
        assertThat( inName, is( item.getCanonicalName() ));
        System.out.println( Strings.padEnd("Similar to " + inName + ":", 24, ' ') + explorerApi.similarIngredients( item, 10) );
    }
}