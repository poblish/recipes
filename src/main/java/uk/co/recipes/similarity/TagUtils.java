/**
 * 
 */
package uk.co.recipes.similarity;

import java.io.Serializable;
import java.util.Map.Entry;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ITag;

import com.google.common.base.Predicate;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class TagUtils {

	public static Predicate<Entry<ITag,Serializable>> findActivated( final CommonTags inTag) {
		return new Predicate<Entry<ITag,Serializable>>() {
			public boolean apply( final Entry<ITag,Serializable> inEntry) {
				return inEntry.getKey() == inTag && ( inEntry.getValue() == Boolean.TRUE || Boolean.parseBoolean((String) inEntry.getValue() ));
			}
		};
	}
}
