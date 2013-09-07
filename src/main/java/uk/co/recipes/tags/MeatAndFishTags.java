/**
 * 
 */
package uk.co.recipes.tags;

import uk.co.recipes.api.ITag;

/**
 * Non-vegetarian items. The main point is to allow vegetarian/non-vegetarian splits using big boosts
 *
 * @author andrewregan
 *
 */
public enum MeatAndFishTags implements ITag {

	MEAT, RED_MEAT, OFFAL, POULTRY, GAME, FISH, SEAFOOD, STOCK, SAUSAGE,

	;

    @Override
    public float getBoost() {
        return 100.0f;
    }
}
