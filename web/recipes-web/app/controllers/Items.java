package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.base.Optional;
import play.mvc.Result;
import service.UserProvider;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.TaggedTasteUpdater;
import uk.co.recipes.external.WikipediaGetter;
import uk.co.recipes.external.WikipediaResults;
import uk.co.recipes.faves.UserFaves;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.ui.CuisineColours;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Items extends AbstractExplorableController {

    private UserRatings ratings;
    private UserFaves faves;
    private ObjectMapper mapper;
    private ISearchAPI searchApi;

    private final static int NUM_RECOMMENDATIONS_TO_SHOW = 12;

    @Inject
    public Items(final TaggedTasteUpdater updater, final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService,
                 final MyrrixRecommendationService inRecService, final EsUserFactory users, final UserRatings inRatings,
                 final ObjectMapper inMapper, final EsSearchService inSearch, final IEventService eventService, final UserFaves userFaves,
                 final CuisineColours colours,
                 final PlayAuthenticate auth, final UserProvider userProvider) {
        super( items, explorerFilters, inExplorerService, inRecService, eventService, colours, auth, userProvider);

        updater.startListening();
        this.ratings = checkNotNull(inRatings);
        this.mapper = checkNotNull(inMapper);
        this.searchApi = checkNotNull(inSearch);
        this.faves = checkNotNull(userFaves);
    }

    public Result display( final String name) throws IOException {
        final IUser user1 = getLocalUser();

        final Optional<ICanonicalItem> optItem = items.get( name.replace('+', ' ') );
        if (!optItem.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final ICanonicalItem item = optItem.get();
        if ( user1 != null) {
            events.visit( user1, item);
        }

        final int recipesCount = searchApi.countRecipesByItemName( item.getCanonicalName() );  // FIXME Should cache this!

        final List<ICanonicalItem> similarities = explorer.similarIngredients( item, getExplorerFilter(explorerFilters), 12);
        final List<IRecipe> recRecipes = ( user1 != null) ? recsApi.recommendRecipes( user1, NUM_RECOMMENDATIONS_TO_SHOW, item) : recsApi.recommendRecipesToAnonymous( NUM_RECOMMENDATIONS_TO_SHOW, item);

        if (!recRecipes.isEmpty()) {
            return ok(views.html.item.render( item, user1, similarities, recRecipes, /* Got recommendations OK */ true, colours, -1, auth, userProvider));
        }

        return ok(views.html.item.render( item, user1, similarities,
                searchApi.findRandomRecipesByItemName( NUM_RECOMMENDATIONS_TO_SHOW, item), /* Didn't get recommendations */ false,
                colours, recipesCount, auth, userProvider));
    }

    public Result rate( final String name, final int inScore) throws IOException {
        final ICanonicalItem item = items.get(name).get();

        final IUser user1 = getLocalUser();
        if ( user1 == null) {
            return unauthorized("Not logged-in");
        }

        ratings.addRating( user1, new ItemRating( item, inScore) );

        return redirect("/items/" + URLEncoder.encode( item.getCanonicalName(), "utf-8"));  // FIXME - horrible way to reload!
    }

    public Result fave( final String name) throws IOException, InterruptedException {
        final ICanonicalItem item = items.get(name).get();

        final IUser user1 = getLocalUser();
        if ( user1 == null) {
            return unauthorized("Not logged-in");
        }

        faves.faveItem( user1, item);

        items.waitUntilRefreshed();

        return redirect("/items/" + URLEncoder.encode( item.getCanonicalName(), "utf-8"));  // FIXME - horrible way to reload!
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