/**
 *
 */
package uk.co.recipes.api;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IIngredient extends INoteworthy {

    ICanonicalItem getItem();
    IQuantity getQuantity();

    boolean isOptional();  // Is it optional at all (leave it's pair for now)
}