/**
 *
 */
package uk.co.recipes.api.ratings;

import uk.co.recipes.api.ICanonicalItem;


/**
 * @author andrewr
 */
public interface IItemRating extends IRating {

    ICanonicalItem getItem();
}
