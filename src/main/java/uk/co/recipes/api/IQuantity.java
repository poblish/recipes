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
public interface IQuantity {

	double getNumber();
	IUnit getUnits();

	String buildString( boolean display);
}
