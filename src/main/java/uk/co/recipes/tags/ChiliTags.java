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
public enum ChiliTags implements ITag {

	SCOVILLE, HEAT_5

	;

    @Override
    public float getBoost() {
        return 1.0f;
    }
}
