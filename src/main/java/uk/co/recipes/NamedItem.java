/**
 * 
 */
package uk.co.recipes;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.INamedItem;

import com.google.common.base.Objects;

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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( canonicalItem, name);
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
		if (!(obj instanceof NamedItem)) {
			return false;
		}
		final NamedItem other = (NamedItem) obj;
		return Objects.equal( canonicalItem, other.canonicalItem) && Objects.equal( name, other.name);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "name", name)
						.add( "canonical", canonicalItem)
						.toString();
	}
}
