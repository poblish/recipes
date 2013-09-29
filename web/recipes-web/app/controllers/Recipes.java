package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import play.mvc.Result;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.Recipe;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.User;
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
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.tags.NationalCuisineTags;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private ObjectMapper mapper;

    @Inject
    public Recipes( final MyrrixUpdater updater, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService, final EsItemFactory items,
                    final EsRecipeFactory recipes, final EsUserFactory users, final UserRatings inRatings, final MyrrixRecommendationService inRecService, final MetricRegistry metrics,
                    final ObjectMapper inMapper) {
    	super( items, explorerFilters, inExplorerService, inRecService, metrics);

    	updater.startListening();
        this.recipes = checkNotNull(recipes);
        this.ratings = checkNotNull(inRatings);
        this.mapper = checkNotNull(inMapper);
    }

    public Result create() throws IOException, InterruptedException {
		final IUser user1 = getLocalUser();
		final IRecipe recipe = getSessionCreatedRecipe();

        return ok(views.html.create_recipe.render(user1, recipe, recsApi.recommendRecipesToAnonymous( recipe, 12)));
    }

    public Result createAddIngredient( final String ingredient) throws IOException, InterruptedException {
		final IRecipe recipe = getSessionCreatedRecipe();

		final ICanonicalItem item = items.get(ingredient).get();
		if (!recipe.containsItem(item)) {
			recipe.addIngredients( new Ingredient( item, new Quantity( Units.GRAMMES, 10)) );
			storeSessionCreatedRecipe(recipe);
		}

    	return redirect("/recipes/create");
    }

    public Result createRemoveIngredient( final String ingredient) throws IOException, InterruptedException {
		final IRecipe recipe = getSessionCreatedRecipe();
        final ICanonicalItem theItemToRemove = checkNotNull( items.get(ingredient).get(), "Could not load Item");

		recipe.removeItems(theItemToRemove);
		storeSessionCreatedRecipe(recipe);

    	return redirect("/recipes/create");
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

        return ok(views.html.recipe.render( recipe, categorisation, explorer.similarRecipes( recipe, getExplorerFilter(explorerFilters), 12), recsApi.recommendIngredients( recipe, 12) ));
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

    private IRecipe getSessionCreatedRecipe() throws IOException {
    	final String payload = session("recipe_json");
		// System.out.println("> Payload " + payload);

    	final Recipe recipe;

    	if ( payload == null) {
    		recipe = new Recipe( new User("temp_" + System.currentTimeMillis(), "???"), "Untitled", Locale.UK /* FIXME */);
    		recipe.addStage( new RecipeStage() );
    		// System.out.println("Created: " + recipe);
    		storeSessionCreatedRecipe(recipe);
    	}
    	else {
    		recipe = mapper.readValue( payload, Recipe.class);
    		// System.out.println("Parsed: " + recipe);
    	}

    	return recipe;
    }

	private void storeSessionCreatedRecipe( final IRecipe recipe) throws JsonProcessingException {
		// System.out.println("> Storing: " + recipe);
		session().put( "recipe_json", mapper.writeValueAsString(recipe));
	}
}