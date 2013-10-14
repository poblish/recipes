/**
 * 
 */
package uk.co.recipes.faves;

import java.io.IOException;

import javax.inject.Inject;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsUserFactory;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class UserFaves {

    @Inject EsUserFactory userFactory;
    @Inject IEventService eventService;

//    private final static float INDIFFERENT_RATING = 5.0f;
//
    public void faveItem( final IUser inUser, final ICanonicalItem inItem) throws IOException {
    	inUser.addFave(inItem);

    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.faveItem(inUser, inItem);
    }

    public void faveRecipe( final IUser inUser, final IRecipe inRecipe) throws IOException {
    	inUser.addFave(inRecipe);

    	userFactory.update(inUser);

    	// Yes, the rating will be published even if persistence fails. Is that a problem???
    	eventService.faveRecipe(inUser, inRecipe);
    }

//    public Optional<IRecipeRating> addRating( final IUser inUser, final IRecipeRating inRating) throws IOException {
//    	final Optional<IRecipeRating> oldRating = inUser.addRating(inRating);
//    	final float oldRatingVal = oldRating.isPresent() ? oldRating.get().getScore() : INDIFFERENT_RATING;
//
//    	userFactory.update(inUser);
//
//    	// Yes, the rating will be published even if persistence fails. Is that a problem???
//    	eventService.rateRecipe( inUser, inRating.getRecipe(), ratingToScore( inRating.getScore() ) - ratingToScore(oldRatingVal));
//    	return oldRating;
//    }
//
//    private float ratingToScore( final float inRating) {
//    	return /* Attempt to 'penalise' low ratings */ 2 * ( inRating - INDIFFERENT_RATING);
//    }
}