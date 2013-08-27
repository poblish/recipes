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
public enum CommonTags implements ITag {

	SERVES_COUNT, VEGETARIAN, VEGAN, NUT_FREE, VEGETABLE, OIL, SALAD_LEAF, FAT, DAIRY,

	// Spices are obtained from roots, flowers, fruits, seeds or bark
	SPICE, // Cinnamon, Ginger, Cloves, Saffron, Nutmeg, Vanilla, Cumin

	// Herbs are obtained from the leaves of herbaceous (non-woody) plants.
	HERB, // Thyme, Sage, Oregano, Parsley, Marjoram, Basil, Chives, Rosemary, Mint

	SUGAR, SALT, FLOUR, BREAD, PASTA, SEED, NUT, PULSE, FRUIT, CITRUS, CHEESE, CHILLI, EGG,

	MEAT, OFFAL, POULTRY, GAME, FISH, SEAFOOD, STOCK, SAUSAGE,

	WINE, ALCOHOL, VINEGAR, SAUCE, CONDIMENT, RICE, NOODLES, WHEAT, GRAIN,

	FLAVOURING, BAKING,

	// Should be in FlavourTags
	ANISEED,

	// Should be in NationalCuisineTags...
	INDIAN, CHINESE, JAPANESE, THAI, VIETNAMESE, FRENCH, ITALIAN, GREEK, ENGLISH, SPANISH, HUNGARIAN, MEXICAN, GERMAN
}
