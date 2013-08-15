/**
 * 
 */
package uk.co.recipes.ratings;

import java.io.IOException;

import javax.inject.Inject;

import uk.co.recipes.api.IUser;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsUserFactory;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class UserRatings {

    @Inject
    EsUserFactory userFactory;

    @Inject
    IEventService eventService;

    public void addRating( final IUser inUser, final IItemRating inRating) throws IOException {
    	inUser.addRating(inRating);
    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.rateItem( inUser, inRating.getItem(), (float) inRating.getScore());
    }

    public void addRating( final IUser inUser, final IRecipeRating inRating) throws IOException {
    	inUser.addRating(inRating);
    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.rateRecipe( inUser, inRating.getRecipe(), (float) inRating.getScore());
    }
}
