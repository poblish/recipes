/**
 * 
 */
package uk.co.recipes;

import java.util.Arrays;
import java.util.Collection;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipeStage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeStage implements IRecipeStage {

	private final Collection<IIngredient> ingredients = Lists.newArrayList();

	// Purely for Jackson deserialization
	public RecipeStage() {
	}

	public void addIngredient( final IIngredient ingredient) {
		ingredients.add(ingredient);
	}

	public void addIngredients( final IIngredient... inIngredients) {
		ingredients.addAll( Arrays.asList(inIngredients) );
	}

	public void addIngredients( final Collection<IIngredient> inIngredients) {
		ingredients.addAll(inIngredients);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipeStage#getIngredients()
	 */
	@Override
	public Collection<IIngredient> getIngredients() {
		return ingredients;
	}

	@JsonIgnore
    @Override
    public Collection<ICanonicalItem> getItems() {
        return FluentIterable.from(ingredients).transform( new Function<IIngredient,ICanonicalItem>() {

            @Override
            public ICanonicalItem apply( final IIngredient input) {
                return input.getItem();
            }} ).toSet();
    }

	public void setIngredients( final Collection<IIngredient> inIngredients) {
		ingredients.addAll(inIngredients);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(ingredients);
	}

	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RecipeStage)) {
			return false;
		}

		final RecipeStage other = (RecipeStage) obj;
		return Objects.equal( ingredients, other.ingredients);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "ingredients", ingredients)
						.toString();
	}
}