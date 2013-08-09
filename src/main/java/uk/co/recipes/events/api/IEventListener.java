/**
 * 
 */
package uk.co.recipes.events.api;

import uk.co.recipes.events.impl.ItemEvent;
import uk.co.recipes.events.impl.RecipeEvent;


/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IEventListener {

	void startListening();

    void onAddItem( final ItemEvent evt);
    void onRateItem( final ItemEvent evt);
    void onDeleteItem( final ItemEvent evt);

    void onAddRecipe( final RecipeEvent evt);
    void onRateRecipe( final RecipeEvent evt);
    void onDeleteRecipe( final RecipeEvent evt);
}