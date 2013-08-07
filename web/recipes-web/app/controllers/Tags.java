package controllers;

import java.io.IOException;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.tags.CommonTags;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Tags extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
    private final static ISearchAPI SEARCH = GRAPH.get( EsSearchService.class );

    public static Result display( final String name) throws IOException {
        final ITag theTag = CommonTags.valueOf( name.toUpperCase() );
        final List<ICanonicalItem> results = SEARCH.findItemsByTag(theTag);
        return ok(views.html.tags.render( theTag, results));
    }
}