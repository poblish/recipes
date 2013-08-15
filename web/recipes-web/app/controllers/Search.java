package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
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

    @Inject
    public Search( final EsSearchService inSearch) {
        this.search = checkNotNull(inSearch);
    }

    public Result doSearch() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());

        if (!gotInput) {
            // Yuk, FIXME
            return ok(views.html.search.render( null, null));
        }

        return ok(views.html.search.render( search.findRecipesByName( theInputs[0] ), search.findItemsByName( theInputs[0] ) ));
    }
}