package uk.co.recipes.service.api;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;

import java.io.IOException;

/**
 * TODO
 *
 * @author andrewr
 */
public interface IRecipePersistence extends IPersistenceAPI<IRecipe> {

    IRecipe fork(final IRecipe inOriginalRecipe, final String inNewName) throws IOException;

    void addIngredients(final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException;
    void removeIngredients(final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException;
    void removeItems(final IRecipe inRecipe, final ICanonicalItem... inItems) throws IOException;
}