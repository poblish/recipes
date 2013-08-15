package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.tags.CommonTags;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Tags extends Controller {

	private ISearchAPI search;

    @Inject
    public Tags( final EsSearchService inSearch) {
        this.search = checkNotNull(inSearch);
    }

    public Result display( final String name) throws IOException {
        final ITag theTag = CommonTags.valueOf( name.toUpperCase() );
        final List<ICanonicalItem> results = search.findItemsByTag(theTag);
        return ok(views.html.tags.render( theTag, results));
    }
}