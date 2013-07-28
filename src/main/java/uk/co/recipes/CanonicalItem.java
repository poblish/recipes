/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItem implements ICanonicalItem {

	private String canonicalName;
	private ICanonicalItem parent = null; // Can't use Optional<> as it screws with JSON serialization
	public Collection<String> aliases = Sets.newHashSet();

	private Map<ITag,Serializable> tags = new TreeMap<>();  // Try to keep the order regular. This will *not* sort enums by name, only by index

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	@JsonCreator
	public CanonicalItem(@JsonProperty("canonicalName") String canonicalName,
						 @JsonDeserialize(as=CanonicalItem.class) @JsonProperty("parent") ICanonicalItem parent) {
		this.canonicalName = canonicalName;

		if ( parent != null && /* Jackson!!! */ parent.getCanonicalName() != null) {
			this.parent = parent;
		}
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName, final Optional<ICanonicalItem> inParent) {
		this.canonicalName = canonicalName;
		parent = inParent.orNull();  // Yuk!
	}

	@Override
	public long getId() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#canonicalName()
	 */
	@Override
	public String getCanonicalName() {
		return canonicalName;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#parent()
	 */
	@Override
	public Optional<ICanonicalItem> parent() {
		return Optional.fromNullable(parent);
	}

	@Override
	public void addTag( final ITag key, final Serializable value) {
		tags.put( key, checkNotNull( value, "Value cannot be null"));
	}

	@Override
	public void addTag( final ITag key) {
		tags.put( key, Boolean.TRUE);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ITagging#tagValues()
	 */
	@Override
	public Map<ITag,Serializable> getTags() {
		if ( parent == null) {
			return tags;
		}

		final Map<ITag,Serializable> allTags = new TreeMap<>();  // Try to keep the order regular. This will *not* sort enums by name, only by index
		allTags.putAll( parent.getTags() );

		for ( Entry<ITag,Serializable> each : tags.entrySet()) {
			if ( each.getValue() == Boolean.FALSE) {
				allTags.remove( each.getKey() );  // Do not inherit!
			}
			else {
				allTags.put( each.getKey(), each.getValue());
			}
		}

		return allTags;
	}

	public Collection<String> getAliases() {
		return this.aliases;
	}

	// Strictly for Jackson only. *Can* be private
	@SuppressWarnings("unused")
	private void setTags( Map<ITag,Serializable> inTags) {
		tags.clear();

		for ( Entry<ITag,Serializable> each : inTags.entrySet()) {
			if (each.getValue() == null) {
				System.err.println("Jackson deserialized null!");
				tags.put( each.getKey(), Boolean.TRUE);
			}
			else if (each.getValue().equals("true")) /* Ugh!!! */ {
				tags.put( each.getKey(), Boolean.TRUE);
			}
			else {
				tags.put( each.getKey(), each.getValue());
			}
		}
	}

	// Strictly for Jackson only. Must be public
	public CanonicalItem getParent() {
		return (CanonicalItem) parent;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#setParent(uk.co.recipes.api.ICanonicalItem)
	 */
	@Override
	public void setParent( ICanonicalItem inParent) {
		parent = checkNotNull( inParent, "Cannot set parent to null");
	}

    @Override
    public boolean descendsFrom( final ICanonicalItem inOther) {
        if (this.equals(inOther)) {
            return true;
        }

        if ( parent != null) {
            return parent.descendsFrom(inOther);
        }

        return false;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( parent, canonicalName);  // Ignoring varieties
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
		if (!(obj instanceof CanonicalItem)) {
			return false;
		}
		final CanonicalItem other = (CanonicalItem) obj;
		return Objects.equal( canonicalName.toLowerCase(), other.canonicalName.toLowerCase()) && Objects.equal( parent, other.parent);
	}

	public String toString() {
		final Map<ITag,Serializable> tags = getTags();

		return Objects.toStringHelper(this).omitNullValues()
						.add( "name", canonicalName)
						.add( "parent", parent)
						.add( "tags", tags.isEmpty() ? null : tags)
						.toString();
	}
}
