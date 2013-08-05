package controllers;

import java.io.IOException;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Users extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
    private final static IUserPersistence USERS = GRAPH.get( EsUserFactory.class );
    private final static IRecommendationsAPI RECS_API = GRAPH.get( MyrrixRecommendationService.class );

    public static Result display( final String id) throws IOException {
        final IUser user = USERS.get(id).get();
        final List<IRecipe> recommendedRecipes = RECS_API.recommendRecipes( user, 10);
        final List<ICanonicalItem> recommendedItems = RECS_API.recommendIngredients( user, 10);
        return ok(views.html.user.render( user, recommendedRecipes, recommendedItems));
    }
}