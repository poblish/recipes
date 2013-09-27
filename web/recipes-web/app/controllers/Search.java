package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.service.impl.EsSearchService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Search extends Controller {

	private ObjectMapper mapper;
	private ISearchAPI search;

    private static final List<IRecipe> EMPTY_RECIPES = Collections.emptyList();
    private static final List<ITag> EMPTY_TAGS = Collections.emptyList();
    private static final List<ICanonicalItem> EMPTY_ITEMS = Collections.emptyList();

    @Inject
    public Search( final EsSearchService inSearch, final ObjectMapper inMapper) {
        this.mapper = checkNotNull(inMapper);
        this.search = checkNotNull(inSearch);
    }

    public Result doSearch() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());

        if (!gotInput) {
            return ok(views.html.search.render( "-", EMPTY_RECIPES, EMPTY_TAGS, EMPTY_ITEMS));
        }

        final String termToUse = theInputs[0].trim();
        return ok(views.html.search.render( "'" + termToUse + "'", search.findRecipesByName(termToUse), search.findTagsByName(termToUse), search.findItemsByName(termToUse) ));
    }

    public Result findPartial() throws IOException {
        final String[] searchString = request().queryString().get("q");
        final String[] count = request().queryString().get("count");
        final String[] callback = request().queryString().get("callback");
        // System.out.println("x = " + Arrays.toString(searchString) + " / " + Arrays.toString(count) + " / " + Arrays.toString(callback));

        final List<ISearchResult<?>> results = search.findPartial( searchString[0], Integer.parseInt( count[0] ));  // FIXME Use 20 if not set, or set > 20
    	// System.out.println("results = " + mapper.writeValueAsString(results));

        // Play 2.1.x doesn't support Jackson 2.x, so we can't pass JsonNode

    	if ( callback.length > 0) {
    		return ok( callback[0] + "(" + mapper.writeValueAsString(results) + ")").as("application/json");
    	}

    	return ok( mapper.writeValueAsString(results) ).as("application/json");
    }
}