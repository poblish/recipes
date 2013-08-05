/**
 * 
 */
package uk.co.recipes.tags;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;

import uk.co.recipes.api.ITag;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class TagUtils {

	private static final Function<Entry<ITag,Serializable>,ITag> ENTRY_KEYS = new Function<Entry<ITag,Serializable>,ITag>() {
		public ITag apply( Entry<ITag,Serializable> input) {
			return input.getKey();
		}
	};

	private static final Function<Entry<ITag,Serializable>,String> TAG_NAMES_TITLECASE = new Function<Entry<ITag,Serializable>,String>() {
		public String apply( Entry<ITag,Serializable> input) {
			return CaseFormat.UPPER_UNDERSCORE.to( CaseFormat.UPPER_CAMEL, input.getKey().toString());
		}
	};

	private static final Predicate<Entry<ITag,Serializable>> ALL_ACTIVATED = new Predicate<Entry<ITag,Serializable>>() {
		public boolean apply( final Entry<ITag,Serializable> inEntry) {
			return ( inEntry.getValue() == Boolean.TRUE || Boolean.parseBoolean((String) inEntry.getValue() ));
		}
	};

	public static Predicate<Entry<ITag,Serializable>> findActivated( final ITag... inTags) {
		final Collection<ITag> tagsColl = Arrays.asList(inTags);
		return new Predicate<Entry<ITag,Serializable>>() {
			public boolean apply( final Entry<ITag,Serializable> inEntry) {
				return tagsColl.contains( inEntry.getKey() ) && ( inEntry.getValue() == Boolean.TRUE || Boolean.parseBoolean((String) inEntry.getValue() ));
			}
		};
	}

	public static Predicate<Entry<ITag,Serializable>> findActivated() {
		return ALL_ACTIVATED;
	}

	public static Function<Entry<ITag,Serializable>,ITag> entryKeys() {
		return ENTRY_KEYS;
	}

	public static Function<Entry<ITag,Serializable>,String> tagNamesTitleCase() {
		return TAG_NAMES_TITLECASE;
	}

	public static ITag forName( final String inName) {
//		try {
			return CommonTags.valueOf(inName);
//		}
//		catch (IllegalArgumentException e) {
//			return NationalCuisineTags.valueOf(inName);
//		}
	}

	public static Comparator<ITag> comparator() {
		return new Comparator<ITag>() {

			@Override
			public int compare( ITag o1, ITag o2) {
				// Yuk, FIXME
				return ComparisonChain.start().compare( o1.toString(), o2.toString()).result();
			}
		};
	}
}