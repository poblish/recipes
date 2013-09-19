package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import play.mvc.Result;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.Units;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.tags.NationalCuisineTags;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.Multiset;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Recipes extends AbstractExplorableController {

    private IRecipePersistence recipes;
    private UserRatings ratings;

    @Inject
    public Recipes( final MyrrixUpdater updater, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService, final EsItemFactory items,
                    final EsRecipeFactory recipes, final EsUserFactory users, final UserRatings inRatings, final MetricRegistry metrics) {
    	super( items, explorerFilters, inExplorerService, metrics);

    	updater.startListening();
        this.recipes = checkNotNull(recipes);
        this.ratings = checkNotNull(inRatings);
    }

    public Result fork( final String name) throws IOException, InterruptedException {
        final String[] newName = request().queryString().get("newName");
        final boolean gotNewName = ( newName != null && newName.length > 0 && !newName[0].isEmpty());
        if (!gotNewName) {
            return status( 400, "New name not set!");
        }

        final IRecipe fork = recipes.fork( recipes.get(name).get(), newName[0]);

        // Wait until it appears in Elasticsearch!
        do {
        	Thread.sleep(250);
        }
        while (!recipes.getById( fork.getId() ).isPresent());

        return reloadRecipe(fork);
    }

    public Result display( final String name) throws IOException {
        final Optional<IRecipe> optRecipe = recipes.get(name);
        if (!optRecipe.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final IRecipe recipe = optRecipe.get();
        final Multiset<ITag> categorisation = Categorisation.forIngredients( recipe.getIngredients(), NationalCuisineTags.values());

        return ok(views.html.recipe.render( recipe, categorisation, explorer.similarRecipes( recipe, getExplorerFilter(explorerFilters), 10)));
    }

    public Result rate( final String name, final int inScore) throws IOException, InterruptedException {
    	return handleLoggedInRecipeBasedAction( name, new RecipeAction() {

			@Override
			public Result doAction( final IUser loggedInUser, final IRecipe recipe) throws IOException, InterruptedException {
				ratings.addRating( loggedInUser, new RecipeRating( recipe, inScore) );
				  
		        return reloadRecipe(recipe);
			}
		} );
    }

    public Result removeIngredient( final String name, final String ingredient) throws IOException, InterruptedException {
    	return handleLoggedInRecipeBasedAction( name, new RecipeAction() {

			@Override
			public Result doAction( final IUser loggedInUser, final IRecipe recipe) throws IOException, InterruptedException {
		        // FIXME Check recipe.getForkDetails(), also creator vs. current user for permissions!

		        final ICanonicalItem theItemToRemove = checkNotNull( items.get(ingredient).get(), "Could not load Item");

		        recipes.removeItems( recipe, theItemToRemove);
		        recipes.waitUntilRefreshed();

		        return reloadRecipe(recipe);
			}
		} );
     }

    public Result addIngredient( final String name, final String ingredient) throws IOException, InterruptedException {
    	return handleLoggedInRecipeBasedAction( name, new RecipeAction() {

			@Override
			public Result doAction( final IUser loggedInUser, final IRecipe recipe) throws IOException, InterruptedException {
		        // FIXME Check recipe.getForkDetails(), also creator vs. current user for permissions!

		        final ICanonicalItem theItemToAdd = checkNotNull( items.get(ingredient).get(), "Could not load Item");

		        recipes.addIngredients( recipe, new Ingredient( theItemToAdd, /* FIXME: hardcoded: */ new Quantity( Units.GRAMMES, 100)));
		        recipes.waitUntilRefreshed();

		        return reloadRecipe(recipe);
			}
		} );
     }

    private Result handleLoggedInRecipeBasedAction( final String inRecipeName, final RecipeAction inAction) throws IOException, InterruptedException {
		final IUser user1 = getLocalUser();
		if ( user1 == null) {
		    return unauthorized("Not logged-in");
		}

        final Optional<IRecipe> optRecipe = recipes.get(inRecipeName);
        if (!optRecipe.isPresent()) {
            return notFound("'" + inRecipeName + "' not found!");
        }

        return inAction.doAction( user1, optRecipe.get() );
    }

    private Result reloadRecipe( final IRecipe inRecipe) {
        return redirect("/recipes/" + inRecipe.getTitle());  // FIXME - horrible way to reload!
    }

    private interface RecipeAction {
		Result doAction( IUser loggedInUser, IRecipe recipe) throws IOException, InterruptedException;
    }
}