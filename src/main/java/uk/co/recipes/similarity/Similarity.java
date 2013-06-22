/**
 * 
 */
package uk.co.recipes.similarity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;

import com.google.common.collect.Iterables;

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

		return between( inA.getTags(), inB.getTags());
	}

	@SuppressWarnings("unchecked")
	public static double between( final Map<ITag,Serializable> inA, final Map<ITag,Serializable> inB) throws IncompatibleIngredientsException {
		final Entry<ITag,Serializable>[] ia = Iterables.toArray( inA.entrySet(), Entry.class);
		final Entry<ITag,Serializable>[] ib = Iterables.toArray( inB.entrySet(), Entry.class);
		SimilarityAggregator aggr = new SimilarityAggregator();  // I think...

		for ( int i = 0; i < ia.length; i++) {
			for ( int j = 0; j < ib.length; j++) {
				aggr.record( between( ia[i], ib[j]) );
			}
		}

		return aggr.aggregate();
	}

	public static double between( final Entry<ITag,Serializable> inA, final Entry<ITag,Serializable> inB) throws IncompatibleIngredientsException {
		if (( inA.getKey() == CommonTags.MEAT && inB.getKey() == CommonTags.VEGETARIAN) || ( inB.getKey() == CommonTags.MEAT && inA.getKey() == CommonTags.VEGETARIAN) ) {  // FIXME Should check values too!
			throw new IncompatibleIngredientsException();
		}

		if (( inA.getKey() == CommonTags.MEAT && inB.getKey() == CommonTags.VEGAN) || ( inB.getKey() == CommonTags.MEAT && inA.getKey() == CommonTags.VEGAN) ) {  // FIXME Should check values too!
			throw new IncompatibleIngredientsException();
		}

		if ( inA.getKey() == inB.getKey()) {  // FIXME Should check values too!
			return 1.0;
		}

		return 0;
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

			return similarity / (double) count;
		}
	}
}
