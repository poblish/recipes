package uk.co.recipes.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.MoreObjects;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.tags.RecipeTags;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CuisineColours {

    @Inject
    @Named("cuisineColours")
    Map<String,String> cuisineColours;

    @Inject
    public CuisineColours() {
        // For Dagger
    }

    public String colourForName( final String inName) {
    	return cuisineColours.get( checkNotNull(inName) );
    }

    public String colourForRecipe( final IRecipe inRecipe) {
    	final String cuisineName = MoreObjects.firstNonNull((String) inRecipe.getTags().get( RecipeTags.RECIPE_CUISINE ), "");
    	return cuisineName.isEmpty() ? "" : cuisineColours.get(cuisineName);
    }

    public Map<String,String> getMap() {
    	return Collections.unmodifiableMap(cuisineColours);
    }
}
