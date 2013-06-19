/**
 * 
 */
package uk.co.recipes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class Recipe implements IRecipe {

	private final List<IRecipeStage> stages = Lists.newArrayList();
	private final Map<ITag,Serializable> tagsMap = Maps.newHashMap();

	public void addStage( final RecipeStage inStage) {
		stages.add(inStage);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#ingredients()
	 */
	@Override
	public Collection<IIngredient> ingredients() {
		final Collection<IIngredient> is = Sets.newHashSet();

		for ( IRecipeStage eachStage : stages) {
			// FIXME is.addAll( eachStage.);
		}

		return is;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#stages()
	 */
	@Override
	public List<IRecipeStage> stages() {
		return stages;
	}

	@Override
	public void addTag( final ITag key, final Serializable value) {
		tagsMap.put( key, value);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ITagging#tagValues()
	 */
	@Override
	public Map<ITag,Serializable> getTags() {
		return tagsMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( stages, tagsMap);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Recipe)) {
			return false;
		}
		final Recipe other = (Recipe) obj;
		return Objects.equal( stages, other.stages) && Objects.equal( tagsMap, other.tagsMap);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "stages", stages)
						.add( "tags", tagsMap)
						.toString();
	}
}
