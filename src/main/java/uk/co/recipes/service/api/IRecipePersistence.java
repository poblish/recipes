package uk.co.recipes.service.api;

import java.io.IOException;
import uk.co.recipes.api.IRecipe;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IRecipePersistence extends IPersistenceAPI<IRecipe> {

    IRecipe fork( final IRecipe inOriginalRecipe) throws IOException;
}