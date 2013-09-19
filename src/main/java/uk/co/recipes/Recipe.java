/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.co.recipes.tags.TagUtils.findActivated;
import static uk.co.recipes.tags.TagUtils.tagNamesTitleCase;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.Preconditions;
import org.joda.time.DateTime;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IForkDetails;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class Recipe implements IRecipe {

    private static final long serialVersionUID = 1L;

	private static final long UNSET_ID = 0x3FFFFFFFFFFFFFFFL;  // Halfway to Long.MAX_VALUE
	public static final long BASE_ID = UNSET_ID + 1L;

	private long id = UNSET_ID;

	private IUser creator;
	private String title;
	private Locale locale;
	private DateTime creationTime = new DateTime();
	private IForkDetails forkDetails;

	private final List<IRecipeStage> stages = Lists.newArrayList();
	private final Map<ITag,Serializable> tagsMap = Maps.newHashMap();

	// Purely for Jackson deserialization
	public Recipe() {
	}

	public Recipe( final IUser inCreator, String inTitle, final Locale inLocale) {
		creator = checkNotNull( inCreator, "Creator cannot be null");
		title = checkNotNull( inTitle, "Title cannot be null");
		locale = checkNotNull( inLocale, "Locale cannot be null");
	}

	public Recipe( final IUser inCreator, String inTitle, final Locale inLocale, final DateTime inCreationTime) {
		this( inCreator, inTitle,  inLocale);
		creationTime = checkNotNull( inCreationTime, "CreationTime cannot be null");
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

    @Override
    public void setTitle( final String inTitle) {
        title = checkNotNull( inTitle, "Title cannot be null");
    }

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	public void addStage( final IRecipeStage inStage) {
		stages.add(inStage);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#ingredients()
	 */
	@Override
	public Collection<IIngredient> getIngredients() {
		final Collection<IIngredient> is = Sets.newHashSet();

		for ( IRecipeStage eachStage : stages) {
			is.addAll( eachStage.getIngredients() );
		}

		return is;
	}

	@JsonIgnore
	@Override
    public Collection<ICanonicalItem> getItems() {
        final Collection<ICanonicalItem> is = Sets.newLinkedHashSet();

        for ( IRecipeStage eachStage : stages) {
            is.addAll( eachStage.getItems() );
        }

        return is;
    }

    public boolean containsAllOf( final ICanonicalItem... inOthers) {
        for ( final ICanonicalItem eachInclusion : inOthers) {
            boolean found = false;
	        for ( ICanonicalItem eachItem : getItems()) {
                if (eachItem.descendsFrom(eachInclusion)) {
                    found = true;
                    break;
                }
	        }

	        if (!found) {
	        	return false;
	        }
	    }

        return true;
    }

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#stages()
	 */
	@Override
	public List<IRecipeStage> getStages() {
		return stages;
	}

	@Override
	public void addTag( final ITag key, final Serializable value) {
		tagsMap.put( key, checkNotNull( value, "Value cannot be null"));
	}

	@Override
	public void addTag( final ITag key) {
		tagsMap.put( key, Boolean.TRUE);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ITagging#tagValues()
	 */
	@Override
	public Map<ITag,Serializable> getTags() {
		return tagsMap;
	}

	@JsonIgnore  // Prevent Jackson insanity
	@Override
	public List<String> getTagNamesForDisplay() {
		return FluentIterable.from( getTags().entrySet() ).filter( findActivated() ).transform( tagNamesTitleCase() ).toList();
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#removeItems(uk.co.recipes.api.ICanonicalItem[])
	 */
	@Override
	public boolean removeItems( ICanonicalItem... inItems) {
		boolean result = false;
		for ( final IRecipeStage each : stages) {
			result |= each.removeItems(inItems);
		}
		return result;
	}

	@Override
	public boolean addIngredients( final IIngredient... inIngredients) {
		boolean result = false;
		for ( final IRecipeStage each : stages) {
			result |= each.addIngredients(inIngredients);
		}
		return result;
	}

	@Override
	public boolean removeIngredients( final IIngredient... inIngredients) {
		boolean result = false;
		for ( final IRecipeStage each : stages) {
			result |= each.removeIngredients(inIngredients);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IRecipe#getCreator()
	 */
	@Override
	public IUser getCreator() {
		return creator;
	}

	@Override
	public IForkDetails getForkDetails() {
		return forkDetails;
	}

	@Override
	public void setForkDetails( final IForkDetails inForkDetails) {
		// Can't check null - probably called by Jackson
		forkDetails = inForkDetails;
	}

	@Override
	public DateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public long getId() {
		return id;
	}

    // Strictly for Jackson only. Must be public
    public Collection<String> getAutoCompleteTerms() {
        return Lists.newArrayList( getTitle() );  // Jackson barfs if we use Collections.singletonList()
    }

    @Override
	public void setId( long inId) {
		if ( id == UNSET_ID && inId == UNSET_ID) {
			// Let Jackson off...
			return;
		}

		Preconditions.checkArgument( inId >= BASE_ID, "New Id must be >= " + BASE_ID + " [" + inId +"]");
		// Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
		id = inId;
	}

    public Object clone() {
        final Recipe theClone = new Recipe(creator, title, locale, creationTime);
        for (IRecipeStage eachStage : stages) {
            theClone.addStage(eachStage);
        }
        for (Entry<ITag, Serializable> eachTag : tagsMap.entrySet()) {
            theClone.addTag(eachTag.getKey(), eachTag.getValue());
        }

        theClone.setForkDetails( getForkDetails() );

        return theClone;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( title, locale, stages, tagsMap, creator, /* creationTime, */ forkDetails);
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
		if (!(obj instanceof Recipe)) {
			return false;
		}

		final Recipe other = (Recipe) obj;
		return Objects.equal( title, other.title) && Objects.equal( locale, other.locale) &&
			   Objects.equal( stages, other.stages) && Objects.equal( tagsMap, other.tagsMap) &&
			   Objects.equal( creator, other.creator) && /* Objects.equal( creationTime, other.creationTime) && */
			   Objects.equal( forkDetails, other.forkDetails);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "title", title)
						.add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
						.add( "creator", creator)
						.add( "fork", forkDetails)
						.add( "stages", stages)
						.add( "tags", tagsMap)
						.add( "locale", locale)
						.toString();
	}
}
