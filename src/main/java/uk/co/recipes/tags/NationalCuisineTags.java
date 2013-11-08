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
public enum NationalCuisineTags implements ITag {

	INDIAN, CHINESE, JAPANESE, THAI, VIETNAMESE, FRENCH, ITALIAN, GREEK, ENGLISH, SPANISH, HUNGARIAN, MEXICAN, GERMAN, SWISS,
	DUTCH, USA, CARIBBEAN, TUNISIAN, MOROCCAN, IRANIAN, RUSSIAN, SCANDINAVIAN, TURKISH, MIDDLE_EASTERN, NORTH_AFRICAN, SE_ASIAN, SOUTH_ASIAN,

	ASIAN

	;

    @Override
    public float getBoost() {
        return 1.0f;
    }
}
