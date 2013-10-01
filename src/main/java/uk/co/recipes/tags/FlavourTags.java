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
	BRAMBLE_HEDGE, FLORAL_FRUITY, ROASTED, MEATY, /* Already got... CHEESY, */ EARTHY, MUSTARDY, SULPHUROUS, MARINE, BRINE_SALT, GREEN_GRASSY, SPICY, WOODLAND, FRESH_FRUITY, CREAMY_FRUITY, CITRUS /* Originally 'CITRUSSY' */

	;

    @Override
    public float getBoost() {
        return 1.0f;
    }
}
