/**
 *
 */
package uk.co.recipes.tags;

import uk.co.recipes.api.ITag;

/**
 * TODO
 *
 * @author andrewregan
 */
public enum RecipeTags implements ITag {

    COOKTIME, PREPTIME, TOTALTIME, RECIPE_CUISINE, RECIPE_CATEGORY, SERVES_COUNT, VEGETARIAN, VEGAN, NUT_FREE;

    @Override
    public float getBoost() {
        // Half-baked attempt to group recipes together, so vegan ones don't get mixed with non-vegan ones
        return 100.0f;
    }
}
