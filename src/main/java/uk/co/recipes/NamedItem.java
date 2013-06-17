/**
 * 
 */
package uk.co.recipes;

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
		name = canonicalItem.canonicalName();
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
	public String name() {
		return name;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.INamedItem#canonicalItem()
	 */
	@Override
	public ICanonicalItem canonicalItem() {
		return canonicalItem;
	}
}
