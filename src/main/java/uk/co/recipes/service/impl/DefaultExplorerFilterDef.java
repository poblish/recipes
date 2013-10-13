/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.Collections;
import java.util.Set;

import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.IExplorerFilterDef;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class DefaultExplorerFilterDef implements IExplorerFilterDef {

	private Set<ITag> includeTags;
	private Set<ITag> excludeTags;

	public DefaultExplorerFilterDef( @JsonProperty("includeTags") final Set<ITag> includeTags, @JsonProperty("excludeTags") final Set<ITag> excludeTags) {
		this.includeTags = includeTags;
		this.excludeTags = excludeTags;
	}

	@Override
	public Set<ITag> getIncludeTags() {
		if ( includeTags != null) {
			return includeTags;
		}
		return Collections.emptySet();
	}

	@Override
	public Set<ITag> getExcludeTags() {
		if ( excludeTags != null) {
			return excludeTags;
		}
		return Collections.emptySet();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode( includeTags, excludeTags);
	}

	@Override
	public boolean equals( final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DefaultExplorerFilterDef)) {
			return false;
		}
		final DefaultExplorerFilterDef other = (DefaultExplorerFilterDef) obj;
		return Objects.equal( includeTags, other.includeTags) && Objects.equal( excludeTags, other.excludeTags);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).omitNullValues().add( "includeTags", includeTags).add( "excludeTags", excludeTags).toString();
	}
}