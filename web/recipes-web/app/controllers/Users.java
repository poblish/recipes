package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import jgravatar.Gravatar;
import play.mvc.Controller;
import play.mvc.Result;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.faves.UserFaves;
import uk.co.recipes.myrrix.MyrrixPrefsIngester;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.ui.CuisineColours;

import com.codahale.metrics.MetricRegistry;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Users extends Controller {

    private IUserPersistence users;
    private MetricRegistry metrics;
    private UserFaves faves;
    private IRecommendationsAPI recsApi;
    private CuisineColours colours;
    private MyrrixPrefsIngester prefsIngester;

    @Inject
    public Users( final EsUserFactory inUsers, final MetricRegistry metrics, final UserFaves userFaves,
    			  final MyrrixRecommendationService inRecService, final CuisineColours colours, final MyrrixPrefsIngester inIngester) {
        this.users = checkNotNull(inUsers);
        this.metrics = checkNotNull(metrics);
        this.faves = checkNotNull(userFaves);
        this.recsApi = checkNotNull(inRecService);
        this.colours = checkNotNull(colours);
        this.prefsIngester = checkNotNull(inIngester);
    }

    public Result display( final String id) throws IOException {
        final IUser user = users.get(id).get();
        final Gravatar gravatar = new Gravatar();
        gravatar.setSize(256);

        final List<IRecipe> recommendedRecipes = recsApi.recommendRecipes( user, 12);
        final List<ICanonicalItem> recommendedItems = recsApi.recommendIngredients( user, 12);
        return ok(views.html.user.render( user, recommendedRecipes, recommendedItems, colours, gravatar.getUrl( user.getEmail() )));
    }

    public Result ingestPrefs() throws IOException {
    	final File theFile = new File( /* FIXME Hardcoded: */ "/Users/andrewregan/Development/java/recipe_explorer/src/test/resources/recommendations1.yaml");

    	final String data = prefsIngester.parseRecommendations(theFile);
    	prefsIngester.ingestRecommendations(data);

    	final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return unauthorized("Not logged-in");  // Surely shouldn't happen...
        }

    	for ( ICanonicalItem eachItem : prefsIngester.parseFaves(theFile)) {
    		faves.faveItem( currUser, eachItem);
    	}

//      items.waitUntilRefreshed();

    	prefsIngester.parseBlocks(theFile);  // FIXME - do something!

    	return ok("true");
    }

    private IUser getLocalUser() {
        return /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( metrics, session());
    }
}