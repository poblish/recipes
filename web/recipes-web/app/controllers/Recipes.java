package controllers;

import static com.google.common.base.Preconditions.checkNotNull;
import com.codahale.metrics.MetricRegistry;
import java.io.IOException;
import javax.inject.Inject;
import play.mvc.Result;
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
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
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

    private IItemPersistence items;
    private IRecipePersistence recipes;
    private IExplorerAPI explorer;
    private EsExplorerFilters explorerFilters;
    private UserRatings ratings;

    @Inject
    public Recipes( final MyrrixUpdater updater, final EsExplorerFilters explorerFilters, final MyrrixExplorerService explorer, final EsItemFactory items,
                    final EsRecipeFactory recipes, final EsUserFactory users, final UserRatings inRatings, final MetricRegistry metrics) {
    	super(metrics);

    	updater.startListening();
        this.items = checkNotNull(items);
        this.recipes = checkNotNull(recipes);
        this.explorer = checkNotNull(explorer);
        this.explorerFilters = checkNotNull(explorerFilters);
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
        final Optional<IRecipe> optRecipe = recipes.get(name);
        if (!optRecipe.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final IRecipe recipe = optRecipe.get();
        final Multiset<ITag> categorisation = Categorisation.forIngredients( recipe.getIngredients() );

        return ok(views.html.recipe.render( recipe, categorisation, explorer.similarRecipes( recipe, getExplorerFilter(explorerFilters), 10)));
    }

    public Result rate( final String name, final int inScore) throws IOException, IncompatibleIngredientsException {
		final IUser user1 = getLocalUser();
		if ( user1 == null) {
		    return unauthorized("Not logged-in");
		}

        final Optional<IRecipe> optRecipe = recipes.get(name);
        if (!optRecipe.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final IRecipe recipe = optRecipe.get();
		ratings.addRating( user1, new RecipeRating( recipe, inScore) );
  
        return redirect("/recipes/" + recipe.getTitle());  // FIXME - horrible way to reload!
    }

    public Result removeIngredient( final String name, final String ingredient) throws IOException, IncompatibleIngredientsException, InterruptedException {
		final IUser user1 = getLocalUser();
		if ( user1 == null) {
		    return unauthorized("Not logged-in");
		}

        final Optional<IRecipe> optRecipe = recipes.get(name);
        if (!optRecipe.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final IRecipe recipe = optRecipe.get();

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