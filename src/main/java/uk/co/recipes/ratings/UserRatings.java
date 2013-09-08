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

import com.google.common.base.Optional;

/**
 * NB. We need to send the current - previous because Myrrix is additive: sending 5/10 twice would count as +5 +5 = +10
 *
 * @author andrewregan
 *
 */
public class UserRatings {

    @Inject
    EsUserFactory userFactory;

    @Inject
    IEventService eventService;

    private final static float INDIFFERENT_RATING = 5.0f;

    public Optional<IItemRating> addRating( final IUser inUser, final IItemRating inRating) throws IOException {
    	final Optional<IItemRating> oldRating = inUser.addRating(inRating);
    	final float oldRatingVal = oldRating.isPresent() ? oldRating.get().getScore() : INDIFFERENT_RATING;

    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.rateItem( inUser, inRating.getItem(), ratingToScore( inRating.getScore() ) - ratingToScore(oldRatingVal));
    	return oldRating;
    }

    public Optional<IRecipeRating> addRating( final IUser inUser, final IRecipeRating inRating) throws IOException {
    	final Optional<IRecipeRating> oldRating = inUser.addRating(inRating);
    	final float oldRatingVal = oldRating.isPresent() ? oldRating.get().getScore() : INDIFFERENT_RATING;

    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.rateRecipe( inUser, inRating.getRecipe(), ratingToScore( inRating.getScore() ) - ratingToScore(oldRatingVal));
    	return oldRating;
    }

    private float ratingToScore( final float inRating) {
    	return /* Attempt to 'penalise' low ratings */ 2 * ( inRating - INDIFFERENT_RATING);
    }
}