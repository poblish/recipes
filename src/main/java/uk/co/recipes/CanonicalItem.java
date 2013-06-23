/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
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
	private Collection<ICanonicalItem> varieties = Sets.newHashSet();

	private Map<ITag,Serializable> tags = Maps.newHashMap();

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
	public CanonicalItem(String canonicalName, Collection<ICanonicalItem> varieties) {
		this.canonicalName = canonicalName;

		for ( final ICanonicalItem eachVariety : varieties) {
			addVariety(eachVariety);
		}
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	@JsonCreator
	public CanonicalItem(@JsonProperty("canonicalName") String canonicalName,
						 @JsonDeserialize(as=CanonicalItem.class) @JsonProperty("parent") ICanonicalItem parent,
						 @JsonProperty("varieties") Collection<ICanonicalItem> varieties) {
		this.canonicalName = canonicalName;

		if ( parent != null && /* Jackson!!! */ parent.getCanonicalName() != null) {
			this.parent = parent;
//			this.tags.putAll( parent.getTags() );
		}

		if ( varieties != null) {
			for ( final ICanonicalItem eachVariety : varieties) {
				addVariety(eachVariety);
			}
		}
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName, ICanonicalItem parent) {
		this.canonicalName = canonicalName;
		setParent(parent);
//		this.tags.putAll( parent.getTags() );
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

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#addVariety(uk.co.recipes.api.ICanonicalItem)
	 */
	@Override
	public void addVariety( ICanonicalItem variety) {
		variety.setParent(this);
		varieties.add(variety);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#varieties()
	 */
	@Override
	public Collection<ICanonicalItem> varieties() {
		return varieties;
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
		final Map<ITag,Serializable> allTags = Maps.newHashMap(tags);

		if ( parent != null) {
			allTags.putAll( parent.getTags() );
		}

		return allTags;
	}

	// Strictly for Jackson only
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
		return Objects.equal( canonicalName.toLowerCase(), other.canonicalName.toLowerCase()) && Objects.equal( parent, other.parent);  // Ignoring varieties
	}

	public String toString() {
		final Map<ITag,Serializable> tags = getTags();

		return Objects.toStringHelper(this).omitNullValues()
						.add( "name", canonicalName)
						.add( "parent", parent)
						.add( "tags", tags.isEmpty() ? null : tags)
//						.add( "num_varieties", varieties.size())
						.toString();
	}
}
