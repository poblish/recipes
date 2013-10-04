/**
 * 
 */
package uk.co.recipes.cats;

import static uk.co.recipes.tags.TagUtils.entryKeys;
import static uk.co.recipes.tags.TagUtils.findActivated;

import java.util.Collection;
import java.util.List;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.tags.TagUtils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public final class Categorisation {

    private Categorisation() {}

	public static Multiset<ITag> forIngredients( final Collection<IIngredient> inIngredients) {
		final Multiset<ITag> tagsSet = TreeMultiset.create( TagUtils.comparator() );

		for ( IIngredient each : inIngredients) {
			tagsSet.addAll( FluentIterable.from( each.getItem().getTags().entrySet() ).filter( findActivated() ).transform( entryKeys() ).toList() );
		}

		return tagsSet;
	}

	public static Multiset<ITag> forIngredients( final Collection<IIngredient> inIngredients, final ITag... inTagsToCheck) {
		final Multiset<ITag> tagsSet = TreeMultiset.create( TagUtils.comparator() );

		for ( IIngredient each : inIngredients) {
			tagsSet.addAll( FluentIterable.from( each.getItem().getTags().entrySet() ).filter( findActivated(inTagsToCheck) ).transform( entryKeys() ).toList() );
		}

		return tagsSet;
	}

    // As above, but optionals are only weighted 0.5
    public static Multiset<ITag> forIngredientsWeighted( final Collection<IIngredient> inIngredients, final ITag... inTagsToCheck) {
        final Multiset<ITag> tagsSet = TreeMultiset.create( TagUtils.comparator() );

        for ( IIngredient each : inIngredients) {
            final List<ITag> theTags = FluentIterable.from( each.getItem().getTags().entrySet() ).filter( findActivated(inTagsToCheck) ).transform( entryKeys() ).toList();
            tagsSet.addAll(theTags);

            if (!each.isOptional()) {  // Add twice for non-optional
                tagsSet.addAll(theTags);
            }
        }

        return tagsSet;
    }
}
