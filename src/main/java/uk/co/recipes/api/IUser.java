/**
 * 
 */
package uk.co.recipes.api;

import org.joda.time.DateTime;
import java.util.Collection;
import java.util.Set;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IUser extends java.io.Serializable {

	long getId();
	void setId( long id);

    String getUserName();
    String getFirstName();
    String getLastName();
    String getDisplayName();

    String getEmail();
    void setEmail( String email);
    boolean getEmailValidated();

    boolean isActive();
    DateTime getLastLoginTime();

    void addAuth( IUserAuth auth);
    boolean removeAuth( IUserAuth auth);
    Set<IUserAuth> getAuths();

    void addRating( IItemRating inRating);
	Collection<IItemRating> getItemRatings();

    void addRating( IRecipeRating inRating);
    Collection<IRecipeRating> getRecipeRatings();

//    void removeRating( IRating inRating);
}