/**
 * 
 */
package uk.co.recipes.api;

import com.google.common.base.Optional;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ICanonicalItem extends ITagging {

	String getCanonicalName();

	Optional<ICanonicalItem> parent();

	void setParent( ICanonicalItem canonicalItem);
}