/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsearch.common.Preconditions;

import uk.co.recipes.api.IUser;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class User implements IUser {

	private final static long UNSET_ID = -1L;

	private long id = UNSET_ID;

    private String username;
    private String displayName;
    private final Collection<IItemRating> itemRatings = Sets.newHashSet();  // FIXME Be careful loading this, could be big
    private final Collection<IRecipeRating> recipeRatings = Sets.newHashSet();  // FIXME Be careful loading this, could be big

    @JsonCreator
    public User( @JsonProperty("userName") final String inUName, @JsonProperty("displayName") final String inDName) {
        username = checkNotNull(inUName);
        displayName = checkNotNull(inDName);
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

		Preconditions.checkArgument( inId >= 0, "New Id must be >= 0 [" + inId +"]");
		Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
		id = inId;
	}

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<IItemRating> getItemRatings() {
        return itemRatings;  // FIXME Be careful loading this, could be big
    }

    @Override
    public Collection<IRecipeRating> getRecipeRatings() {
        return recipeRatings;  // FIXME Be careful loading this, could be big
    }

    @Override
    public void addRating(IItemRating inRating) {
        itemRatings.add(inRating);
    }

    @Override
    public void addRating(IRecipeRating inRating) {
        recipeRatings.add(inRating);
    }

//    @Override
//    public void removeRating(IRating inRating) {
//        ratings.remove(inRating);
//    }

	@Override
	public int hashCode() {
		return Objects.hashCode( id, username, displayName);
	}

	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		final User other = (User) obj;
		return ( id == other.id) && Objects.equal( username, other.username) && Objects.equal( displayName, other.displayName);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
                        .add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
                        .add( "username", getUserName())
                        .add( "displayName", getDisplayName())
						.toString();
	}
}