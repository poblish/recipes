/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;
import uk.co.recipes.api.ratings.IRating;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IUser {

	long getId();
	void setId( long id);

	String getUserName();
    String getDisplayName();

	Collection<IRating> getRatings();
    void addRating( IRating inRating);
    void removeRating( IRating inRating);
}