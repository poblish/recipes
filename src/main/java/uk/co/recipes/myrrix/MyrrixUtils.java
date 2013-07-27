/**
 * 
 */
package uk.co.recipes.myrrix;

import java.util.List;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixUtils {

	public static List<Long> getItems( final List<RecommendedItem> inItems) {
		System.out.println(inItems);
		return FluentIterable.from(inItems).transform( new Function<RecommendedItem,Long>() {

			@Override
			public Long apply( RecommendedItem input) {
				return input.getItemID();
			}
		} ).toList();
	}
}
