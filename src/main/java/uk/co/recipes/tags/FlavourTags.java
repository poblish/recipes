/**
 * 
 */
package uk.co.recipes.tags;

import uk.co.recipes.api.ITag;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public enum FlavourTags implements ITag {

	ANISEED, SWEET, SOUR, ORANGE, LEMON, LIME, ALMOND, COFFEE

	;

    @Override
    public float getBoost() {
        return 1.0f;
    }
}
