/**
 *
 */
package uk.co.recipes.tags;

import uk.co.recipes.api.ITag;

/**
 * Non-vegetarian items.
 *
 * @author andrewregan
 */
public enum MeatAndFishTags implements ITag {

    MEAT, RED_MEAT, WHITE_MEAT, OFFAL, POULTRY, GAME, FISH, SEAFOOD, SAUSAGE,;

    @Override
    public float getBoost() {
        return 50.0f;
    }
}
