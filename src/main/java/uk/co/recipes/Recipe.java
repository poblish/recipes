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
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.elasticsearch.common.Preconditions;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;

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

	private final static long UNSET_ID = 0x3FFFFFFFFFFFFFFFL;  // Halfway to Long.MAX_VALUE
	public final static long BASE_ID = UNSET_ID + 1L;

	private long id = UNSET_ID;
	private String title;
	private final List<IRecipeStage> stages = Lists.newArrayList();
	private final Map<ITag,Serializable> tagsMap = Maps.newHashMap();

	// Purely for Jackson deserialization
	public Recipe() {
	}

	public Recipe( String inTitle) {
		title = checkNotNull( inTitle, "Title cannot be null");
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	public void addStage( final RecipeStage inStage) {
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
        final Collection<ICanonicalItem> is = Sets.newHashSet();

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

	@Override
	public List<String> getTagNamesForDisplay() {
		return FluentIterable.from( getTags().entrySet() ).filter( findActivated() ).transform( tagNamesTitleCase() ).toList();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId( long inId) {
		if ( id == UNSET_ID && inId == UNSET_ID) {
			// Let Jackson off...
			return;
		}

		Preconditions.checkArgument( inId >= BASE_ID, "New Id must be >= " + BASE_ID + " [" + inId +"]");
		Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
		id = inId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( title, stages, tagsMap);
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
		return Objects.equal( title, other.title) && Objects.equal( stages, other.stages) && Objects.equal( tagsMap, other.tagsMap);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "title", title)
						.add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
						.add( "stages", stages)
						.add( "tags", tagsMap)
						.toString();
	}
}
