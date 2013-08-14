/**
 * 
 */
package uk.co.recipes.events.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;

import com.google.common.base.Objects;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeRemoveItemsEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final IRecipe recipe;
    private final ICanonicalItem item;

    public RecipeRemoveItemsEvent( final IRecipe inRecipe, final ICanonicalItem inItem) {
    	recipe = checkNotNull(inRecipe);
    	item = checkNotNull(inItem);
    }

	public IRecipe getRecipe() {
		return recipe;
	}

	public ICanonicalItem getItem() {
		return item;
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "recipe", recipe.getId())
						.add( "item", item)
						.toString();
	}
}
