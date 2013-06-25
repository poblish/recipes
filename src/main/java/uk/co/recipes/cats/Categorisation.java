/**
 * 
 */
package uk.co.recipes.cats;

import static uk.co.recipes.similarity.TagUtils.entryKeys;
import static uk.co.recipes.similarity.TagUtils.findActivated;

import java.util.Collection;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
import uk.co.recipes.similarity.TagUtils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class Categorisation {

	public static Multiset<ITag> forIngredients( final Collection<IIngredient> inIngredients) throws IncompatibleIngredientsException {
		final Multiset<ITag> tagsSet = TreeMultiset.create( TagUtils.comparator() );

		for ( IIngredient each : inIngredients) {
			tagsSet.addAll( FluentIterable.from( each.getItem().getCanonicalItem().getTags().entrySet() ).filter( findActivated() ).transform( entryKeys() ).toList() );
		}

		return tagsSet;
	}
}
