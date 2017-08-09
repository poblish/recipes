package controllers;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import play.mvc.Result;
import service.UserProvider;
import uk.co.recipes.*;
import uk.co.recipes.api.*;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.faves.UserFaves;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IExplorerFilterDef;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.tags.RecipeTags;
import uk.co.recipes.ui.CuisineColours;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private UserFaves faves;
	private ObjectMapper mapper;

	public static final String[] RECIPE_CATS = {"Afternoon tea", "Breakfast", "Brunch", "Buffet", "Canapes", "Condiment", "Dessert", "Dinner", "Drink", "Fish Course", "Lunch", "Main course", "Pasta course", "Side dish", "Snack", "Soup course", "Starter", "Supper", "Treat", "Vegetable course"};
	public static final String[] DIET_TYPES = {"Vegan", "Vegetarian"};

    @Inject
    public Recipes(final MyrrixUpdater updater, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService, final EsItemFactory items,
				   final EsRecipeFactory recipes, final UserRatings inRatings, final MyrrixRecommendationService inRecService, final MetricRegistry metrics,
				   final ObjectMapper inMapper, final IEventService eventService, final UserFaves userFaves, final CuisineColours colours,
				   final PlayAuthenticate auth, final UserProvider userProvider) {
    	super( items, explorerFilters, inExplorerService, inRecService, eventService, colours, auth, userProvider);

    	updater.startListening();
        this.recipes = checkNotNull(recipes);
        this.ratings = checkNotNull(inRatings);
        this.mapper = checkNotNull(inMapper);
        this.faves = checkNotNull(userFaves);
    }

    public Result create() throws IOException, InterruptedException {
		final IUser user1 = getLocalUser();
		final IRecipe recipe = getSessionCreatedRecipe(true);
        return ok(views.html.create_recipe.render(user1, recipe, recsApi.recommendRandomRecipesToAnonymous( recipe, 12), colours, auth, userProvider));
    }

    public Result createAddIngredient( final String ingredient) throws IOException, InterruptedException {
		final IRecipe recipe = getSessionCreatedRecipe(true);

		final ICanonicalItem item = items.get(ingredient).get();
		if (!recipe.containsItem(item)) {
			recipe.addIngredients( new Ingredient( item, new Quantity( Units.GRAMMES, 10)) );
			storeSessionCreatedRecipe(recipe);
		}

    	return redirect("/recipes/create");
    }

    public Result createRemoveIngredient( final String ingredient) throws IOException, InterruptedException {
		final IRecipe recipe = getSessionCreatedRecipe(true);
        final ICanonicalItem theItemToRemove = checkNotNull( items.get(ingredient).get(), "Could not load Item");

		recipe.removeItems(theItemToRemove);
		storeSessionCreatedRecipe(recipe);

    	return redirect("/recipes/create");
    }

    public Result clearCreate() throws IOException {
		final IRecipe recipe = getSessionCreatedRecipe(false);

		if ( recipe != null) {
			recipe.removeIngredients( Iterables.toArray( recipe.getIngredients(), IIngredient.class) );  // Yuk!
			storeSessionCreatedRecipe(recipe);
		}

    	return redirect("/recipes/create");
    }

    public Result finishCreate( final String inNewTitle) throws IOException, InterruptedException {
		final IRecipe recipe = getSessionCreatedRecipe(false);
		recipe.setTitle(inNewTitle);

		recipes.put( recipe, null);
		recipes.waitUntilRefreshed();
		clearCreate();

    	return reloadRecipe(recipe);
    }

    public Result cancelCreate() {
		session().remove("recipe_json");
    	return redirect("/");
    }

	public Result fork( final String name) throws IOException, InterruptedException {
        final String[] newName = request().queryString().get("newName");
        final boolean gotNewName = ( newName != null && newName.length > 0 && !newName[0].isEmpty());
        if (!gotNewName) {
            return status( 400, "New name not set!");
        }

        final IRecipe fork = recipes.fork( recipes.get(name).get(), newName[0]);

        recipes.waitUntilRefreshed();

        return reloadRecipe(fork);
    }

    public Result display( final String name) throws IOException {
        final Optional<IRecipe> optRecipe = recipes.get(name);
        if (!optRecipe.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

		final IUser user1 = getLocalUser();
		final IRecipe recipe = optRecipe.get();

		if ( user1 != null) {
			events.visit( user1, recipe);
		}

		final String cuisineName = MoreObjects.firstNonNull((String) recipe.getTags().get( RecipeTags.RECIPE_CUISINE ), "");
		final String cuisineColour = cuisineName.isEmpty() ? "" : colours.colourForName(cuisineName);

        final IExplorerFilterDef filter = getExplorerFilter(explorerFilters);

        return ok(views.html.recipe.render( recipe, user1, explorer.similarRecipes( recipe, filter, 12),
				recsApi.recommendIngredients( recipe, 9), filter, colours, cuisineName, cuisineColour,
				recipeCategoriesSelections(filter), recipeDietTypesSelections(filter),
				auth, userProvider));
    }

	private Map<String,Boolean> recipeCategoriesSelections( final IExplorerFilterDef inFilterDef) {
        final Set<IExplorerFilterItem<?>> items = inFilterDef.getIncludes();

        final Map<String,Boolean> results = Maps.newLinkedHashMap();

        for ( String eachCat : RECIPE_CATS) {
			results.put( eachCat, Boolean.FALSE);

			for ( IExplorerFilterItem<?> eachItem : items) {
        		if (eachItem.getValue().isPresent() && eachItem.getValue().get().equals(eachCat)) {
        			results.put( eachCat, Boolean.TRUE);  // Is selected
        			break;
        		}
        	}
        }

        return results;
	}

	// FIXME - filthy code
	private Map<String,Boolean> recipeDietTypesSelections( final IExplorerFilterDef inFilterDef) {
        final Set<IExplorerFilterItem<?>> items = inFilterDef.getIncludes();

        final Map<String,Boolean> results = Maps.newLinkedHashMap();

        for ( String eachCat : DIET_TYPES) {
			results.put( eachCat, Boolean.FALSE);

			for ( IExplorerFilterItem<?> eachItem : items) {
        		if (eachItem.getEntity().toString().equalsIgnoreCase(eachCat)) {
        			results.put( eachCat, Boolean.TRUE);  // Is selected
        			break;
        		}
        	}
        }

        return results;
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

    // Bear in mind: can be unfave-ing too!
    public Result fave( final String name) throws IOException, InterruptedException {
    	return handleLoggedInRecipeBasedAction( name, new RecipeAction() {

			@Override
			public Result doAction( final IUser loggedInUser, final IRecipe recipe) throws IOException, InterruptedException {
				faves.faveRecipe( loggedInUser, recipe);

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

    private IRecipe getSessionCreatedRecipe( boolean inCreateIfNeeded) throws IOException {
    	final String payload = session("recipe_json");
		// System.out.println("> Payload " + payload);

    	final Recipe recipe;

    	if ( payload == null) {
    		if (!inCreateIfNeeded) {
    			return null;
    		}

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