package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Controller;
import play.mvc.Result;
import service.UserProvider;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.ui.CuisineColours;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.co.recipes.service.api.ESearchArea.ITEMS;
import static uk.co.recipes.service.api.ESearchArea.TAGS;

public class Search extends Controller {

	private ObjectMapper mapper;
	private ISearchAPI search;
    private CuisineColours colours;
    private PlayAuthenticate auth;
    private UserProvider userProvider;

    private static final List<IRecipe> EMPTY_RECIPES = Collections.emptyList();
    private static final List<ITag> EMPTY_TAGS = Collections.emptyList();
    private static final List<ICanonicalItem> EMPTY_ITEMS = Collections.emptyList();

    @Inject
    public Search(final EsSearchService inSearch, final ObjectMapper inMapper, final CuisineColours colours,
                  final PlayAuthenticate auth, final UserProvider userProvider) {
        this.mapper = checkNotNull(inMapper);
        this.search = checkNotNull(inSearch);
        this.colours = checkNotNull(colours);
        this.auth = checkNotNull(auth);
        this.userProvider = checkNotNull(userProvider);
    }

    public Result doSearch() throws IOException {
		final IUser currUser = getLocalUser();
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());

        if (!gotInput) {
            return ok(views.html.search.render( "-", currUser, colours, EMPTY_RECIPES, EMPTY_TAGS, EMPTY_ITEMS, auth, userProvider));
        }

        final String termToUse = theInputs[0].trim();
        return ok(views.html.search.render( "'" + termToUse + "'", currUser, colours,
                search.findRecipesByName( termToUse, 200), search.findTagsByName(termToUse),
                search.findItemsByName(termToUse),
                auth, userProvider));
    }

    public Result findPartial() throws IOException {
        final String[] searchString = request().queryString().get("q");
        final String[] count = request().queryString().get("count");
        // System.out.println("x = " + Arrays.toString(searchString) + " / " + Arrays.toString(count) + " / " + Arrays.toString(callback));

        final List<ISearchResult<?>> results = search.findPartial( searchString[0], Integer.parseInt( count[0] ), ITEMS);  // FIXME Use 20 if not set, or set > 20

    	return returnSearchResults( results, request().queryString().get("callback"));
    }

    public Result findPartialWithTags() throws IOException {
        final String[] searchString = request().queryString().get("q");
        final String[] count = request().queryString().get("count");
        // System.out.println("x = " + Arrays.toString(searchString) + " / " + Arrays.toString(count) + " / " + Arrays.toString(callback));

        final List<ISearchResult<?>> results = search.findPartial( searchString[0], Integer.parseInt( count[0] ), ITEMS, TAGS);  // FIXME Use 20 if not set, or set > 20

    	return returnSearchResults( results, request().queryString().get("callback"));
    }

    private Result returnSearchResults( final List<ISearchResult<?>> inResults, final String[] inCallback) throws IOException {
    	// System.out.println("results = " + mapper.writeValueAsString(results));

        // Play 2.1.x doesn't support Jackson 2.x, so we can't pass JsonNode

    	if ( inCallback.length > 0) {
    		return ok( inCallback[0] + "(" + mapper.writeValueAsString(inResults) + ")").as("application/json");
    	}

    	return ok( mapper.writeValueAsString(inResults) ).as("application/json");
    }

    private IUser getLocalUser() {
        return userProvider.getUser(session());
    }
}