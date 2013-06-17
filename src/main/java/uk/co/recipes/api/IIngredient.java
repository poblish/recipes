/**
 * 
 */
package uk.co.recipes.api;

import java.util.Locale;
import java.util.Map;


/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public interface IIngredient {

	INamedItem item();
	IQuantity quantity();

	void addNote( final Locale inLocale, final String inNote);
	Map<Locale,String> notes();
}