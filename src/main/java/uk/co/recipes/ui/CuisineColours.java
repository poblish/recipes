/**
 * 
 */
package uk.co.recipes.ui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

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

    public String colourForName( final String inName) {
    	return cuisineColours.get( checkNotNull(inName) );
    }

    public Map<String,String> getMap() {
    	return Collections.unmodifiableMap(cuisineColours);
    }
}
