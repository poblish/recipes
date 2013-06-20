/**
 * 
 */
package uk.co.recipes;

import com.google.common.base.Objects;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.INamedItem;

/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public class NamedItem implements INamedItem {

	private final String name;
	private final ICanonicalItem canonicalItem;

	/**
	 * @param name
	 * @param canonicalItem
	 * @param variety
	 */
	public NamedItem(ICanonicalItem canonicalItem) {
		this.canonicalItem = canonicalItem;
		name = canonicalItem.getCanonicalName();
	}

	/**
	 * @param name
	 * @param canonicalItem
	 * @param variety
	 */
	public NamedItem(ICanonicalItem canonicalItem, String name) {
		this.canonicalItem = canonicalItem;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.INamedItem#name()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.INamedItem#canonicalItem()
	 */
	@Override
	public ICanonicalItem getCanonicalItem() {
		return canonicalItem;
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "name", name)
						.add( "canonical", canonicalItem)
						.toString();
	}
}
