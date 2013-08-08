package controllers;

import java.io.IOException;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.User;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.similarity.IncompatibleIngredientsException;

import com.google.common.base.Supplier;
import com.google.common.collect.Multiset;

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

    public static Result display( final String name) throws IOException, IncompatibleIngredientsException {
        final IRecipe recipe = RECIPES.get(name).get();
        final Multiset<ITag> categorisation = Categorisation.forIngredients( recipe.getIngredients() );

        return ok(views.html.recipe.render( recipe, categorisation, EXPLORER_API.similarRecipes( recipe, 10), ( recipe != null) ? "Found" : "Not Found"));
    }

    public static Result test() throws IOException, IncompatibleIngredientsException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());
        final String theInput = gotInput ? theInputs[0] : "inputs3.txt";

        final IRecipe recipe = RECIPES.get(theInput).get();
        final Multiset<ITag> categorisation = Categorisation.forIngredients( recipe.getIngredients() );

        final IUser user1 = USERS.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

            @Override
            public IUser get() {
                return new User( "aregan", "Andrew Regan");
            }
        } );
        return ok(views.html.recipes.render( theInput, recipe, categorisation, EXPLORER_API.similarRecipes( recipe, 10), user1, gotInput ? "Search Results" : "Test"));
    }
}