package uk.co.recipes.service.api;

import java.io.IOException;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IRecipePersistence extends IPersistenceAPI<IRecipe> {

    IRecipe fork( final IRecipe inOriginalRecipe) throws IOException;

    void waitUntilRefreshed() throws InterruptedException;

    void addIngredients( final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException;
    void removeIngredients( final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException;
    void removeItems( final IRecipe inRecipe, final ICanonicalItem... inItems) throws IOException;
}