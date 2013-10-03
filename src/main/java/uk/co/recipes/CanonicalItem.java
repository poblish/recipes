/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.co.recipes.tags.TagUtils.findActivated;
import static uk.co.recipes.tags.TagUtils.tagNamesTitleCase;

import java.util.Set;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.elasticsearch.common.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.ITag;
import uk.co.recipes.tags.TagUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
@JsonInclude( JsonInclude.Include.NON_NULL )  // Mainly to avoid null baseAmount being serialized
public class CanonicalItem implements ICanonicalItem {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger( CanonicalItem.class );

    private static final long UNSET_ID = -1L;
	private static final long TOO_HIGH_ID = 0x4000000000000000L;


	private long id = UNSET_ID;
	private String canonicalName;
	private ICanonicalItem parent = null; // Can't use Optional<> as it screws with JSON serialization
	public Collection<String> aliases = Sets.newHashSet();
	private IQuantity baseAmount;

	private Map<ITag,Serializable> tags = new TreeMap<>( Ordering.usingToString() );  // Try to keep the order regular. This will *not* sort enums by name, only by index
    private Set<ITag> cancelTags = Sets.newHashSet();

    public Collection<ICanonicalItem> constituents = Sets.newHashSet();

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

    public void addCancelTag( final ITag key) {
        cancelTags.add(key);
    }

    // *Has* to be exist, and be public, for Jackson
    public Set<ITag> getCancelTags() {
        return cancelTags;
    }

    // *Has* to be exist, and be public, for Jackson
    public void setCancelTags( Set<ITag> inTags) {
        cancelTags = inTags;
    }

    // Jackson *will* use this to persist 'tags'
	@Override
	public Map<ITag,Serializable> getTags() {
		if ( parent == null) {
			return tags;
		}

		final Map<ITag,Serializable> allTags = new TreeMap<>( Ordering.usingToString() );  // Try to keep the order regular
		allTags.putAll( parent.getTags() );

		for ( Entry<ITag,Serializable> each : tags.entrySet()) {
			allTags.put( each.getKey(), each.getValue());
		}

		for ( ITag eachCancelTag : cancelTags) {
            allTags.remove(eachCancelTag);  // Do not inherit!
		}

		return allTags;
	}

	public boolean hasOverlappingTags() {
	    // *Must* take a copy of the collection - do *not* modify 'tags'
	    final List<ITag> copy = Lists.newArrayList( Maps.filterEntries( tags, TagUtils.findActivated()).keySet() );
	    copy.retainAll( ((CanonicalItem) parent).tags.keySet() );
	    return !copy.isEmpty();
	}

	@JsonIgnore  // Prevent Jackson insanity
	@Override
	public List<String> getTagNamesForDisplay() {
		return FluentIterable.from( getTags().entrySet() ).filter( findActivated() ).transform( tagNamesTitleCase() ).toList();
	}

	public Collection<String> getAliases() {
		return this.aliases;
	}

    public Collection<ICanonicalItem> getConstituents() {
        return this.constituents;
    }

    // Jackson *will* use this to persist 'tags'. *Can* be private
	@SuppressWarnings("unused")
	private void setTags( Map<ITag,Serializable> inTags) {
		tags.clear();

		for ( Entry<ITag,Serializable> each : inTags.entrySet()) {
			if (each.getValue() == null) {
				LOG.warn("Jackson deserialized null!");
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

    // Strictly for Jackson only. Must be public
    public Collection<String> getAutoCompleteTerms() {
        final Collection<String> terms = Sets.newHashSet( this.aliases );
        terms.add( this.canonicalName);
        return terms;
    }

    // Strictly for Jackson only. Must be public
    public void setAutoCompleteTerms( Collection<String> terms) {
    	this.aliases = terms;  // Yuk FIXME
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

	public void setBaseAmount( final IQuantity inQuantity) {
		// Horrible Jackson hack to avoid throwing NPE during deser
		if ( baseAmount == null && inQuantity == null) {
			return;
		}

		baseAmount = checkNotNull(inQuantity);
	}

	public IQuantity getBaseAmount() {
		return baseAmount;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#setId(long)
	 */
	@Override
	public void setId( long inId) {
		if ( id == UNSET_ID && inId == UNSET_ID) {
			// Let Jackson off...
			return;
		}

		Preconditions.checkArgument( inId >= 0 && inId < TOO_HIGH_ID, "New Id must be >= 0 [" + inId +"]");
		Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
		id = inId;
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
		final Map<ITag,Serializable> tagsVal = getTags();

		return Objects.toStringHelper(this).omitNullValues()
						.add( "name", canonicalName)
					//	.add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
						.add( "parent", parent)
						.add( "tags", tagsVal.isEmpty() ? null : tagsVal)
						.toString();
	}
}
