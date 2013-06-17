/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;

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
	private final Map<ITag,String> tagsMap = Maps.newHashMap();

	public void addStage( final RecipeStage inStage) {
		stages.add(inStage);
	}

	public void addTag( final ITag key, final String value) {
		tagsMap.put( key, value);
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

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ITagging#tagValues()
	 */
	@Override
	public Map<ITag,String> tagValues() {
		return tagsMap;
	}
}
