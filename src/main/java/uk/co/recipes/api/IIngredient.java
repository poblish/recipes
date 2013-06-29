/**
 * 
 */
package uk.co.recipes.api;

/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public interface IIngredient extends INoteworthy {

	ICanonicalItem getItem();
	IQuantity getQuantity();
}