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

	public boolean addIngredients( final IIngredient... inIngredients) {
		return ingredients.addAll( Arrays.asList(inIngredients) );
	}

	public boolean addIngredients( final Collection<IIngredient> inIngredients) {
		return ingredients.addAll(inIngredients);
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

	@Override
	public boolean containsItem( final ICanonicalItem inItem) {
        for ( IIngredient each : ingredients) {
            if (each.getItem().equals(inItem)) {
            	return true;
            }
        }
        return false;
	}

	public void setIngredients( final Collection<IIngredient> inIngredients) {
		ingredients.addAll(inIngredients);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipeStage#removeItems(uk.co.recipes.api.ICanonicalItem[])
	 */
	@Override
	public boolean removeItems( final ICanonicalItem... inItems) {
		final Collection<ICanonicalItem> inputs = Arrays.asList(inItems);
		final Collection<IIngredient> deletions = Lists.newArrayList();

		for ( IIngredient each : ingredients) {
			if (inputs.contains( each.getItem() )) {
				deletions.add(each);
			}
		}

		return ingredients.removeAll(deletions);
	}

	@Override
	public boolean removeIngredients( final IIngredient... inIngredients) {
		return ingredients.removeAll( Arrays.asList(inIngredients) );
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