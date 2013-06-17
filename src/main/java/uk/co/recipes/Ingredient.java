/**
 * 
 */
package uk.co.recipes;

import java.util.Locale;
import java.util.Map;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.INamedItem;
import uk.co.recipes.api.IQuantity;

import com.google.common.collect.Maps;

/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public class Ingredient implements IIngredient {

	private INamedItem item;
	private IQuantity quantity;
	private final Map<Locale,String> notes = Maps.newHashMap();

	/**
	 * @param item
	 * @param quantity
	 */
	public Ingredient(INamedItem item, IQuantity quantity) {
		this.item = item;
		this.quantity = quantity;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#item()
	 */
	@Override
	public INamedItem item() {
		return item;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#quantity()
	 */
	@Override
	public IQuantity quantity() {
		return quantity;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#addNote(java.util.Locale, java.lang.String)
	 */
	@Override
	public void addNote( Locale inLocale, String inNote) {
		notes.put( inLocale, inNote);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#notes()
	 */
	@Override
	public Map<Locale,String> notes() {
		return notes;
	}
}
