/**
 * 
 */
package uk.co.recipes.events.api;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IEventService {

    void addListener( final IEventListener inL);

    void addItem( final ICanonicalItem inItem);
    void rateItem( final IUser inUser, final ICanonicalItem inItem);
    void rateItem( final IUser inUser, final ICanonicalItem inItem, float inRating);
    void deleteItem( final ICanonicalItem inItem);

    void faveItem( final IUser inUser, final ICanonicalItem inItem);
    void unFaveItem( final IUser inUser, final ICanonicalItem inItem);

    void addRecipe( final IRecipe inRecipe);
    void rateRecipe( final IUser inUser, final IRecipe inRecipe);
    void rateRecipe( final IUser inUser, final IRecipe inRecipe, float inRating);
    void addRecipeIngredients( final IRecipe inRecipe, final IIngredient... inIngredients);
    void removeRecipeIngredients( final IRecipe inRecipe, final IIngredient... inIngredients);
    void removeRecipeItems( final IRecipe inRecipe, final ICanonicalItem... inItems);
    void deleteRecipe( final IRecipe inRecipe);
}