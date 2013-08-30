/**
 * 
 */
package uk.co.recipes.tags;

import uk.co.recipes.api.ITag;

/**
 * No non-vegetarian items should appear here
 *
 * @author andrewregan
 *
 */
public enum CommonTags implements ITag {

	VEGETABLE, OIL, SALAD_LEAF, FAT, DAIRY,

	// Spices are obtained from roots, flowers, fruits, seeds or bark
	SPICE, // Cinnamon, Ginger, Cloves, Saffron, Nutmeg, Vanilla, Cumin

	// Herbs are obtained from the leaves of herbaceous (non-woody) plants.
	HERB, // Thyme, Sage, Oregano, Parsley, Marjoram, Basil, Chives, Rosemary, Mint

	SUGAR, SALT, FLOUR, BREAD, PASTA, SEED, NUT, PULSE, FRUIT, CITRUS, CHEESE, BLUE_CHEESE, CHILLI, EGG,

	WINE, ALCOHOL, SPIRIT, VINEGAR, SAUCE, CONDIMENT, RICE, NOODLES, WHEAT, GRAIN,

	FLAVOURING, BAKING

	;

    @Override
    public float getBoost() {
        return 1.0f;
    }
}
