/**
 * 
 */
package uk.co.recipes.corr;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static uk.co.recipes.tags.TagUtils.entryKeys;
import static uk.co.recipes.tags.TagUtils.findActivated;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.tags.TagUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class Correlations {

	@Inject
	CanonicalItemFactory itemFactory;

	public Multiset<ICanonicalItem> findCountsWith( final ICanonicalItem... inInclusions) {
	    final Multiset<ICanonicalItem> counts = HashMultiset.create();
		try {
			final Collection<Recipe> all = RecipeFactory.listAll();
			System.out.println(all.size());
			for ( Recipe each : all) {
			    if (!each.containsAllOf(inInclusions)) {
			        continue;
			    }

			    counts.addAll( each.getItems() );
			}

			counts.removeAll( Arrays.asList(inInclusions) );

			// FIXME Can't filter by count or limit this rubbish:
			return Multisets.copyHighestCountFirst(counts);
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	public Multiset<ITag> findTagsWith( final ICanonicalItem... inInclusions) {
		return findTagsWithPredicate( in( Lists.newArrayList(inInclusions) ), inInclusions);
	}

	public Multiset<ITag> findTagsWithout( final ICanonicalItem... inInclusions) {
		return findTagsWithPredicate( not( in( Lists.newArrayList(inInclusions) ) ), inInclusions);
	}

	public Multiset<ITag> findTagsWithPredicate( final Predicate<ICanonicalItem> inPredicate, final ICanonicalItem... inInclusions) {
		final Multiset<ITag> tagsSet = TreeMultiset.create( TagUtils.comparator() );

		try {
			for ( ICanonicalItem each : FluentIterable.from( itemFactory.listAll() ).filter(inPredicate).toList()) {
				tagsSet.addAll( FluentIterable.from( each.getTags().entrySet() ).filter( findActivated() ).transform( entryKeys() ).toList() );
			}
		}
		catch (IOException e) {
			Throwables.propagate(e);
		}

		return Multisets.copyHighestCountFirst(tagsSet);
	}

	public static void findCountsWithout( final ICanonicalItem... inExclusions) {
		
	}
}
