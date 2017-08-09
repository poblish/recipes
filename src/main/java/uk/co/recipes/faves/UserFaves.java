package uk.co.recipes.faves;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsUserFactory;

import javax.inject.Inject;
import java.io.IOException;

public class UserFaves {

    @Inject EsUserFactory userFactory;
    @Inject IEventService eventService;

    @Inject
    public UserFaves() {
        // For Dagger
    }

    public void faveItem(final IUser inUser, final ICanonicalItem inItem) throws IOException {
        boolean unfave = inUser.isFave(inItem);

        if (unfave) {
            inUser.removeFave(inItem);
        } else {
            inUser.addFave(inItem);
        }

        userFactory.update(inUser);

        if (unfave) {
            eventService.unFaveItem(inUser, inItem);
        } else {
            eventService.faveItem(inUser, inItem);
        }
    }

    public void faveRecipe(final IUser inUser, final IRecipe inRecipe) throws IOException {
        boolean unfave = inUser.isFave(inRecipe);

        if (unfave) {
            inUser.removeFave(inRecipe);
        } else {
            inUser.addFave(inRecipe);
        }

        userFactory.update(inUser);

        if (unfave) {
            eventService.unFaveRecipe(inUser, inRecipe);
        } else {
            eventService.faveRecipe(inUser, inRecipe);
        }
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