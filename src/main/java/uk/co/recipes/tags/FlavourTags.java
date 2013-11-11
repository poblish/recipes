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

	ANISEED, SWEET, SOUR, ORANGE, LEMON, LIME, ALMOND, COFFEE, GARLIC,

	// Categories from 'The Flavour Thesaurus'
	FLORAL, EARTHY, TART, TANNIC, SWEET_NUTTY, UMAMI, /* Already got White Fish */ SMOKY_SALTY, SUGAR_SWEET, CREAMY, SPICY /* Diff'ate from 'SPICE' */, WARM, HOT, PEPPERY, MUSTARDY, DARK_RICH, GRASSY, CITRUS /* Originally 'CITRUSSY' */

	;

    @Override
    public float getBoost() {
        return 5.0f;
    }
}
