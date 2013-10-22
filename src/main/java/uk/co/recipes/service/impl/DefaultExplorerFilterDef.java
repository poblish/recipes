/**
 * 
 */
package uk.co.recipes.service.impl;

import java.util.Collections;
import java.util.Set;

import uk.co.recipes.api.IExplorerFilterItem;
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

	private Set<IExplorerFilterItem<?>> includes;
	private Set<IExplorerFilterItem<?>> excludes;

	public DefaultExplorerFilterDef( @JsonProperty("includes") final Set<IExplorerFilterItem<?>> includes, @JsonProperty("excludes") final Set<IExplorerFilterItem<?>> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}

	@Override
	public Set<IExplorerFilterItem<?>> getIncludes() {
		if ( includes != null) {
			return includes;
		}
		return Collections.emptySet();
	}

	@Override
	public Set<IExplorerFilterItem<?>> getExcludes() {
		if ( excludes != null) {
			return excludes;
		}
		return Collections.emptySet();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode( includes, excludes);
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
		return Objects.equal( includes, other.includes) && Objects.equal( excludes, other.excludes);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).omitNullValues().add( "includes", includes).add( "excludes", excludes).toString();
	}
}