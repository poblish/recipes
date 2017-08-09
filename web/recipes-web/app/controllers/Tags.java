package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Controller;
import play.mvc.Result;
import service.UserProvider;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.tags.TagUtils;
import uk.co.recipes.ui.CuisineColours;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


public class Tags extends Controller {

	private ISearchAPI search;
	private CuisineColours colours;
    private PlayAuthenticate auth;
    private UserProvider userProvider;

    @Inject
    public Tags(final EsSearchService inSearch, final CuisineColours inColours,
                final PlayAuthenticate auth, final UserProvider userProvider) {
        this.search = checkNotNull(inSearch);
        this.colours = checkNotNull(inColours);
        this.auth = checkNotNull(auth);
        this.userProvider = checkNotNull(userProvider);
    }

    public Result display( final String name) throws IOException {
        final IUser user1 = getLocalUser();

        final ITag theTag = TagUtils.forName( name.replace( ' ', '_').toUpperCase() );
        final List<ICanonicalItem> results = search.findItemsByTag(theTag);
        return ok(views.html.tags.render( user1, colours, theTag, results, search.findRandomRecipesByTag( 9, theTag), auth, userProvider));
    }

    private IUser getLocalUser() {
        return userProvider.getUser(session());
    }
}