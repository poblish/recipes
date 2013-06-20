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

	INamedItem getItem();
	IQuantity getQuantity();

	void addNote( final Locale inLocale, final String inNote);
	Map<Locale,String> getNotes();
}