package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
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

    @Inject
    public Users( final EsUserFactory inUsers, final MyrrixRecommendationService inRecService, final CuisineColours colours) {
        this.users = checkNotNull(inUsers);
        this.recsApi = checkNotNull(inRecService);
        this.colours = checkNotNull(colours);
    }

    public Result display( final String id) throws IOException {
        final IUser user = users.get(id).get();
        final List<IRecipe> recommendedRecipes = recsApi.recommendRecipes( user, 10);
        final List<ICanonicalItem> recommendedItems = recsApi.recommendIngredients( user, 10);
        return ok(views.html.user.render( user, recommendedRecipes, recommendedItems, colours));
    }
}