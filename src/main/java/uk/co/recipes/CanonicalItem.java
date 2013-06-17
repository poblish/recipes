/**
 * 
 */
package uk.co.recipes;

import java.util.Collection;

import uk.co.recipes.api.ICanonicalItem;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItem implements ICanonicalItem {

	private final String canonicalName;
	private final Optional<ICanonicalItem> parent;
	private Collection<ICanonicalItem> varieties = Sets.newHashSet();

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName) {
		this.canonicalName = canonicalName;
		this.parent = Optional.absent();
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName, Collection<ICanonicalItem> varieties) {
		this.canonicalName = canonicalName;
		this.parent = Optional.absent();
		this.varieties.addAll(varieties);
	}

	/**
	 * @param canonicalName
	 * @param parent
	 * @param varieties
	 */
	public CanonicalItem(String canonicalName, ICanonicalItem parent, Collection<ICanonicalItem> varieties) {
		this.canonicalName = canonicalName;
		this.parent = Optional.of(parent);
		this.varieties.addAll(varieties);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#canonicalName()
	 */
	@Override
	public String canonicalName() {
		return canonicalName;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#parent()
	 */
	@Override
	public Optional<ICanonicalItem> parent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#addVariety(uk.co.recipes.api.ICanonicalItem)
	 */
	@Override
	public void addVariety( ICanonicalItem variety) {
		varieties.add(variety);
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.ICanonicalItem#varieties()
	 */
	@Override
	public Collection<ICanonicalItem> varieties() {
		return varieties;
	}
}
