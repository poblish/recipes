package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;

import play.mvc.Controller;
import play.mvc.Result;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.tags.TagUtils;
import uk.co.recipes.ui.CuisineColours;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Tags extends Controller {

	private ISearchAPI search;
	private MetricRegistry metrics;
	private CuisineColours colours;

    @Inject
    public Tags( final EsSearchService inSearch, final MetricRegistry inMetrics, final CuisineColours inColours) {
        this.search = checkNotNull(inSearch);
        this.metrics = checkNotNull(inMetrics);
        this.colours = checkNotNull(inColours);
    }

    public Result display( final String name) throws IOException {
        final IUser user1 = getLocalUser();

        final ITag theTag = TagUtils.forName( name.replace( ' ', '_').toUpperCase() );
        final List<ICanonicalItem> results = search.findItemsByTag(theTag);
        return ok(views.html.tags.render( user1, colours, theTag, results, search.findRandomRecipesByTag( 9, theTag)));
    }

    private IUser getLocalUser() {
        return /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( metrics, session());
    }
}