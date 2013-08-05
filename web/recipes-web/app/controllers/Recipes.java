package controllers;

import java.io.IOException;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.User;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;

import com.google.common.base.Supplier;

import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Recipes extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
    private final static IRecipePersistence RECIPES = GRAPH.get( EsRecipeFactory.class );
    private final static IUserPersistence USERS = GRAPH.get( EsUserFactory.class );
    private final static IExplorerAPI EXPLORER_API = GRAPH.get( MyrrixExplorerService.class );

    public static Result display( final String name) throws IOException {
        final IRecipe recipe = RECIPES.get(name).get();
        return ok(views.html.recipe.render( recipe, ( recipe != null) ? "Found" : "Not Found"));
    }

    public static Result test() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());
        final String theInput = gotInput ? theInputs[0] : "inputs3.txt";
        final IRecipe recipe = RECIPES.get(theInput).get();
        final List<IRecipe> similarities = EXPLORER_API.similarRecipes( recipe, 10);

        final IUser user1 = USERS.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

            @Override
            public IUser get() {
                return new User();
            }
        } );
        return ok(views.html.recipes.render( theInput, similarities, user1, gotInput ? "Search Results" : "Test"));
    }
}