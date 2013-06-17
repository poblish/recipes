/**
 * 
 */
package uk.co.recipes.api;

import java.util.Collection;

import com.google.common.base.Optional;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ICanonicalItem {

	String canonicalName();

	Optional<ICanonicalItem> parent();
	Collection<ICanonicalItem> varieties();

	void addVariety( ICanonicalItem item);

	void setParent( ICanonicalItem canonicalItem);
}