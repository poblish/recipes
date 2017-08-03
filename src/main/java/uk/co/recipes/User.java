package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.*;
import org.joda.time.DateTime;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.IUserAuth;
import uk.co.recipes.api.IUserPreferences;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class User implements IUser {

    private static final long serialVersionUID = 1L;

	private static final long UNSET_ID = -1L;

	private long id = UNSET_ID;

    private String username;
    private String firstName;
    private String lastName;
    private String displayName;

    private String email;
    private boolean emailValidated;

    private boolean isActive = true;
    private DateTime lastLoginTime;

    private final Collection<IItemRating> itemRatings = Sets.newHashSet();  // FIXME Be careful loading this, could be big
    private final Collection<IRecipeRating> recipeRatings = Sets.newHashSet();  // FIXME Be careful loading this, could be big
    private final Collection<ICanonicalItem> itemFaves = Sets.newHashSet();  // FIXME Be careful loading this, could be big
    private final Collection<IRecipe> recipeFaves = Sets.newHashSet();  // FIXME Be careful loading this, could be big
    private final Collection<ICanonicalItem> excludedItems = Sets.newHashSet();  // OK, will not persist whole thing
    private final Collection<IRecipe> excludedRecipes = Sets.newHashSet();  // OK, will not persist whole thing
	private final Set<IUserAuth> auths = Sets.newHashSet();
	private final IUserPreferences prefs = new UserPreferences();

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

    // @Override
    public void setDisplayName( final String inName) {
        displayName = inName;
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
    public Optional<IItemRating> addRating(IItemRating inRating) {
    	IItemRating prevRating = null;

    	// Yuk, FIXME
    	for ( IItemRating each : itemRatings) {
    		if ( each.getItem().equals( inRating.getItem() )) {
    			prevRating = each;
    			break;
    		}
    	}

		itemRatings.add(inRating);

		if ( prevRating == null) {
    		return Optional.absent();
    	}

		// Remove old rating and return it
    	itemRatings.remove(prevRating);
        return Optional.of(prevRating);
    }

    @Override
    public Optional<IRecipeRating> addRating(IRecipeRating inRating) {
    	IRecipeRating prevRating = null;

    	// Yuk, FIXME
    	for ( IRecipeRating each : recipeRatings) {
    		if ( each.getRecipe().equals( inRating.getRecipe() )) {
    			prevRating = each;
    			break;
    		}
    	}

    	recipeRatings.add(inRating);

		if ( prevRating == null) {
    		return Optional.absent();
    	}

		// Remove old rating and return it
		recipeRatings.remove(prevRating);
        return Optional.of(prevRating);
    }


	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUser#addAuth(uk.co.recipes.api.IUserAuth)
	 */
	@Override
	public void addAuth( final IUserAuth inAuth) {
		auths.add(inAuth);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUser#removeAuth(uk.co.recipes.api.IUserAuth)
	 */
	@Override
	public boolean removeAuth( final IUserAuth inAuth) {
		return auths.remove(inAuth);
	}

	@Override
	public Set<IUserAuth> getAuths() {
		return auths;
	}

    @Override
    public String getFirstName() {
        return firstName;
    }

//    @Override
    public void setFirstName( final String inName) {
        firstName = inName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

//  @Override
    public void setLastName(final String inName) {
        lastName = inName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail( final String inEmail) {
        email = inEmail;
        emailValidated = false;
    }

    @Override
    public boolean getEmailValidated() {
        return emailValidated;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public DateTime getLastLoginTime() {
        return lastLoginTime;
    }

//    @Override
    public void initLastLoginTime() {
        lastLoginTime = new DateTime();
    }

//    @Override
//    public void removeRating(IRating inRating) {
//        ratings.remove(inRating);
//    }

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IUser#getPrefs()
	 */
	@Override
	public IUserPreferences getPrefs() {
		return prefs;
	}

	@Override
	public void addFave( ICanonicalItem item) {
		itemFaves.add( checkNotNull(item) );
	}

	@Override
	public void removeFave( ICanonicalItem item) {
		itemFaves.remove( checkNotNull(item) );
	}

	@Override
	public boolean isFave( ICanonicalItem item) {
		return itemFaves.contains( checkNotNull(item) );
	}

	@Override
	public void addFave( IRecipe recipe) {
		recipeFaves.add( checkNotNull(recipe) );
	}

	@Override
	public void removeFave( IRecipe recipe) {
		recipeFaves.remove( checkNotNull(recipe) );
	}

	@Override
	public boolean isFave( IRecipe recipe) {
		return recipeFaves.contains( checkNotNull(recipe) );
	}

	@Override
	public Collection<ICanonicalItem> getFaveItems() {
		return itemFaves;
	}

	@JsonProperty("excludedItems")
	public Collection<Long> getExcludedItemParentIdsForJackon() {
		return FluentIterable.from(excludedItems).transform(ICanonicalItem::getId).toList();

	}

	@JsonProperty("excludedRecipes")
	public Collection<Long> getExcludedRecipeIdsForJackon() {
		return FluentIterable.from(excludedRecipes).transform(IRecipe::getId).toList();
	}

	@JsonIgnore
	@Override
	public Collection<ICanonicalItem> getExcludedItemParents() {
		return this.excludedItems;
	}

	@JsonIgnore
	@Override
	public Collection<IRecipe> getExcludedRecipes() {
		return this.excludedRecipes;
	}

	@Override
	public Collection<IRecipe> getFaveRecipes() {
		return recipeFaves;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode( id, username, displayName, itemFaves, recipeFaves);
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
		return ( id == other.id) && Objects.equal( username, other.username) && Objects.equal( displayName, other.displayName) &&
									Objects.equal( itemFaves, other.itemFaves) && Objects.equal( recipeFaves, other.recipeFaves) &&
									Objects.equal( excludedItems, other.excludedItems) && Objects.equal( excludedRecipes, other.excludedRecipes);
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
                        .add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
                        .add( "username", getUserName())
                        .add( "displayName", getDisplayName())
                        .add( "itemFaves", itemFaves.isEmpty() ? null : itemFaves)
                        .add( "recipeFaves", recipeFaves.isEmpty() ? null : recipeFaves)
                        .add( "excludedItems", excludedItems.isEmpty() ? null : excludedItems)
                        .add( "excludedRecipes", excludedRecipes.isEmpty() ? null : excludedRecipes)
                        .add( "auths", auths.isEmpty() ? null : auths)
						.toString();
	}
}