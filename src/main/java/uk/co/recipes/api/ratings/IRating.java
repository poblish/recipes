/**
 * 
 */
package uk.co.recipes.api.ratings;

import uk.co.recipes.api.IUser;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IRating {

    IUser getRater();
    int getScore();
}
