/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.MoreObjects;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IQuantity;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public class Ingredient implements IIngredient {

	private ICanonicalItem item;
	private IQuantity quantity;
	private boolean optional = false;
	private final Map<Locale,List<String>> notes = Maps.newHashMap();

	// Purely for Jackson deserialization
	public Ingredient() {
	}

	/**
	 * @param item
	 * @param quantity
	 */
	public Ingredient(ICanonicalItem item, IQuantity quantity) {
		this( item, quantity, false);
	}

	/**
	 * @param item
	 * @param quantity
	 */
	public Ingredient(ICanonicalItem item, IQuantity quantity, boolean isOptional) {
		this.item = item;
		this.quantity = quantity;
		this.optional = isOptional;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#item()
	 */
	@Override
	public ICanonicalItem getItem() {
		return item;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#quantity()
	 */
	@Override
	public IQuantity getQuantity() {
		return quantity;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#addNote(java.util.Locale, java.lang.String)
	 */
	@Override
	public void addNote( Locale inLocale, String inNote) {
		final List<String> listForLocale = notes.get(inLocale);

		if ( listForLocale == null) {
			notes.put( inLocale, Lists.newArrayList(inNote));
		}
		else {
			listForLocale.add(inNote);
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.INoteworthy#addNotes(java.util.Locale, java.util.Collection)
	 */
	@Override
	public void addNotes( Locale inLocale, final Collection<String> inNotes) {
		if (inNotes.isEmpty()) {
			return;
		}

		final List<String> listForLocale = notes.get(inLocale);

		if ( listForLocale == null) {
			notes.put( inLocale, Lists.newArrayList(inNotes));
		}
		else {
			listForLocale.addAll(inNotes);
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#notes()
	 */
	@Override
	public Map<Locale,List<String>> getNotes() {
		return notes;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IIngredient#isOptional()
	 */
	@Override
	public boolean isOptional() {
		return optional;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( item, quantity, optional, notes);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Ingredient)) {
			return false;
		}
		final Ingredient other = (Ingredient) obj;
		return Objects.equal( item, other.item) && Objects.equal( quantity, other.quantity) && Objects.equal( optional, other.optional) && Objects.equal( notes, other.notes);
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
						.add( "q", quantity)
						.add( "item", item)
						.add( "notes", notes.isEmpty() ? null : notes)
						.toString();
	}
}
