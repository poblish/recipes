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
public interface ICanonicalItem extends ITagging, java.io.Serializable {

	long getId();
	void setId( long id);

	String getCanonicalName();
	IQuantity getBaseAmount();

	Optional<ICanonicalItem> parent();

	boolean descendsFrom( final ICanonicalItem inOther);

	void setParent( ICanonicalItem canonicalItem);

	Collection<ICanonicalItem> getConstituents();
}