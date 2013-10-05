package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.external.WikipediaGetter;
import uk.co.recipes.external.WikipediaResults;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Items extends AbstractExplorableController {

    private UserRatings ratings;
	private ObjectMapper mapper;
	private ISearchAPI searchApi;

	private final static int NUM_RECOMMENDATIONS_TO_SHOW = 12;

    @Inject
    public Items( final MyrrixUpdater updater, final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService,
    			  final MyrrixRecommendationService inRecService, final EsUserFactory users, final UserRatings inRatings, final MetricRegistry metrics,
    			  final ObjectMapper inMapper, final EsSearchService inSearch, final IEventService eventService) {
        super( items, explorerFilters, inExplorerService, inRecService, metrics, eventService);

    	updater.startListening();
        this.ratings = checkNotNull(inRatings);
        this.mapper = checkNotNull(inMapper);
        this.searchApi = checkNotNull(inSearch);
    }

    public Result display( final String name) throws IOException {
		final IUser user1 = getLocalUser();

        final Optional<ICanonicalItem> optItem = items.get(name);
        if (!optItem.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final ICanonicalItem item = optItem.get();
		if ( user1 != null) {
			events.visit( user1, item);
		}

        final List<ICanonicalItem> similarities = explorer.similarIngredients( item, getExplorerFilter(explorerFilters), 20);
        final List<IRecipe> recRecipes = ( user1 != null) ? recsApi.recommendRecipes( user1, NUM_RECOMMENDATIONS_TO_SHOW, item) : recsApi.recommendRecipesToAnonymous( NUM_RECOMMENDATIONS_TO_SHOW, item);

        if (!recRecipes.isEmpty()) {
            return ok(views.html.item.render( item, similarities, recRecipes, /* Got recommendations OK */ true));
        }

        return ok(views.html.item.render( item, similarities, searchApi.findRandomRecipesByItemName( NUM_RECOMMENDATIONS_TO_SHOW, item), /* Didn't get recommendations */ false));
    }

    public Result rate( final String name, final int inScore) throws IOException {
        final ICanonicalItem item = items.get(name).get();

        final IUser user1 = getLocalUser();
        if ( user1 == null) {
            return unauthorized("Not logged-in");
        }

		ratings.addRating( user1, new ItemRating( item, inScore) );
  
        return redirect("/items/" + item.getCanonicalName());  // FIXME - horrible way to reload!
    }

    public Result loadExternals( final String name) throws IOException {
        final Optional<ICanonicalItem> optItem = items.get(name);
        if (!optItem.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

		final Optional<WikipediaResults> results = new WikipediaGetter().getResultsFor( optItem.get().getCanonicalName() );

    	if (results.isPresent()) {
    		return ok( mapper.writeValueAsString( results.get() ) ).as("application/json");
    	}
    	else {
    		return ok("").as("application/json");
    	}
    }
}