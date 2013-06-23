/**
 * 
 */
package uk.co.recipes.similarity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * All similarities in the range -1.0 to 1.0, with 1.0 representing perfect similarity
 *
 * @author andrewregan
 *
 */
public class Similarity {

	public static double amongIngredients( final Collection<IIngredient> inA, final Collection<IIngredient> inB) throws IncompatibleIngredientsException {
		final IIngredient[] ia = Iterables.toArray( inA, IIngredient.class);
		final IIngredient[] ib = Iterables.toArray( inB, IIngredient.class);
		SimilarityAggregator aggr = new SimilarityAggregator();

		for ( int i = 0; i < ia.length; i++) {
			for ( int j = 0; j < ib.length; j++) {
				aggr.record( between( ia[i], ib[j]) );
			}
		}

		return aggr.aggregate();
	}

	public static double amongItems( final Collection<ICanonicalItem> inA, final Collection<ICanonicalItem> inB) throws IncompatibleIngredientsException {
		final ICanonicalItem[] ia = Iterables.toArray( inA, ICanonicalItem.class);
		final ICanonicalItem[] ib = Iterables.toArray( inB, ICanonicalItem.class);
		SimilarityAggregator aggr = new SimilarityAggregator();

		for ( int i = 0; i < ia.length; i++) {
			for ( int j = 0; j < ib.length; j++) {
				aggr.record( between( ia[i], ib[j]) );
			}
		}

		return aggr.aggregate();
	}

	public static double between( final IIngredient inA, final IIngredient inB) throws IncompatibleIngredientsException {
		return between( inA.getItem().getCanonicalItem(), inB.getItem().getCanonicalItem());
	}

	public static double between( final ICanonicalItem inA, final ICanonicalItem inB) throws IncompatibleIngredientsException {
		if ( inA.equals(inB)) {
			return 1.0;
		}

		Optional<ICanonicalItem> pa = inA.parent();
		Optional<ICanonicalItem> pb = inB.parent();
		double pScore = 0.8d;

		// System.out.println( inA + " vs. " + inB);

		while (pa.isPresent()) {
			if ( pa.equals(pb)) {
				return pScore;
			}

			if (!pb.isPresent()) {
				break;
			}

			pa = pa.get().parent();
			pb = pb.get().parent();
			pScore *= 0.8d;
		}

		return between( inA.getTags(), inB.getTags());
	}

	public static double between( final Map<ITag,Serializable> inA, final Map<ITag,Serializable> inB) throws IncompatibleIngredientsException {
		if ( inA.isEmpty() || inB.isEmpty()) {
			return 0.0;
		}

		final Set<Entry<ITag,Serializable>> union = Sets.union( inA.entrySet(), inB.entrySet());

		if ( entriesHaveTag( union, CommonTags.MEAT) && ( entriesHaveTag( union, CommonTags.VEGETARIAN) || entriesHaveTag( union, CommonTags.VEGAN))) {
			throw new IncompatibleIngredientsException();
		}

		final int numInTotal = union.size();
		final int numInCommon = Sets.intersection( inA.entrySet(), inB.entrySet()).size();
		final double score = Math.sqrt((double) numInCommon / (double) numInTotal);

		// System.out.println( "  " + inA.entrySet() + " vs. " + inB.entrySet() + " ... " + union + " ... " + numInCommon + " / " + numInTotal + " = " + score);

		return score;
	}

	private static boolean entriesHaveTag( final Set<Entry<ITag,Serializable>> inEntries, final CommonTags inTag) {
		return !FluentIterable.from(inEntries).filter( TagUtils.findActivated(inTag) ).isEmpty();
	}

	private static class SimilarityAggregator {
		double similarity = 0;
		int count = 0;

		public void record( double score) {
			similarity += score;
			count++;
		}

		public double aggregate() {
			if ( count == 0) {
				return 0;
			}

			return Math.sqrt( similarity / (double) count);
		}
	}
}
