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

	INamedItem getItem();
	IQuantity getQuantity();
}