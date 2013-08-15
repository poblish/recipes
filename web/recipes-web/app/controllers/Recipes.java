package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.User;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.similarity.IncompatibleIngredientsException;

import com.google.common.base.Supplier;
import com.google.common.collect.Multiset;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Recipes extends Controller {

    private IItemPersistence items;
    private IRecipePersistence recipes;
    private IUserPersistence users;
    private IExplorerAPI explorer;
    private UserRatings ratings;

    @Inject
    public Recipes( final MyrrixUpdater updater, final MyrrixExplorerService explorer, final EsItemFactory items, final EsRecipeFactory recipes, final EsUserFactory users, final UserRatings inRatings) {
    	updater.startListening();
        this.items = checkNotNull(items);
        this.recipes = checkNotNull(recipes);
        this.users = checkNotNull(users);
        this.explorer = checkNotNull(explorer);
        this.ratings = checkNotNull(inRatings);
    }

    public Result fork( final String name) throws IOException, IncompatibleIngredientsException, InterruptedException {
        final IRecipe fork = recipes.fork( recipes.get(name).get() );

        // Wait until it appears in Elasticsearch!
        do {
        	Thread.sleep(250);
        }
        while (!recipes.getById( fork.getId() ).isPresent());

        return redirect("/recipes/" + fork.getTitle());  // FIXME - horrible title stuff
    }

    public Result display( final String name) throws IOException, IncompatibleIngredientsException {
        final IRecipe recipe = recipes.get(name).get();
        final Multiset<ITag> categorisation = Categorisation.forIngredients( recipe.getIngredients() );

        return ok(views.html.recipe.render( recipe, categorisation, explorer.similarRecipes( recipe, 10), ( recipe != null) ? "Found" : "Not Found"));
    }

    public Result rate( final String name, final int inScore) throws IOException, IncompatibleIngredientsException {
        final IRecipe recipe = recipes.get(name).get();

		final IUser user1 = users.getOrCreate( "Andrew Regan", new Supplier<IUser>() {  // FIXME Need actual user!

			@Override
			public IUser get() {
				return new User( "aregan", "Andrew Regan");
			}
		} );

		ratings.addRating( user1, new RecipeRating( recipe, inScore) );
  
        return redirect("/recipes/" + recipe.getTitle());  // FIXME - horrible way to reload!
    }

    public Result removeIngredient( final String name, final String ingredient) throws IOException, IncompatibleIngredientsException, InterruptedException {
        final IRecipe recipe = recipes.get(name).get();
        // FIXME Check recipe.getForkDetails(), also creator vs. current user for permissions!

        final ICanonicalItem theItemToRemove = checkNotNull( items.get(ingredient).get() );

        recipes.removeItems( recipe, theItemToRemove);
        recipes.waitUntilRefreshed();

        return redirect("/recipes/" + recipe.getTitle());  // FIXME - horrible way to reload!
    }

    public Result test() throws IOException, IncompatibleIngredientsException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());
        return display( gotInput ? theInputs[0] : "inputs3.txt");
    }
}