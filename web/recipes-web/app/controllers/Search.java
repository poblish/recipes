package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Search extends Controller {

	private ISearchAPI search;

    private static final List<IRecipe> EMPTY_RECIPES = Collections.emptyList();
    private static final List<ICanonicalItem> EMPTY_ITEMS = Collections.emptyList();

    @Inject
    public Search( final EsSearchService inSearch) {
        this.search = checkNotNull(inSearch);
    }

    public Result doSearch() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());

        if (!gotInput) {
            return ok(views.html.search.render( EMPTY_RECIPES, EMPTY_ITEMS));
        }

        final String termToUse = theInputs[0].trim();
        return ok(views.html.search.render( search.findRecipesByName(termToUse), search.findItemsByName(termToUse) ));
    }
}