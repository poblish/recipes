package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import jgravatar.Gravatar;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.myrrix.MyrrixPrefsIngester;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.ui.CuisineColours;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Users extends Controller {

    private IUserPersistence users;
    private IRecommendationsAPI recsApi;
    private CuisineColours colours;
    private MyrrixPrefsIngester prefsIngester;

    @Inject
    public Users( final EsUserFactory inUsers, final MyrrixRecommendationService inRecService, final CuisineColours colours, final MyrrixPrefsIngester inIngester) {
        this.users = checkNotNull(inUsers);
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
    	final String data = prefsIngester.parseRecommendations( new File( /* FIXME Hardcoded: */ "/Users/andrewregan/Development/java/recipe_explorer/src/test/resources/recommendations1.yaml") );
    	prefsIngester.ingestRecommendations(data);
    	return ok("true");
    }
}