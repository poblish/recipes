/**
 * 
 */
package uk.co.recipes.service.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import uk.co.recipes.api.ITag;
import uk.co.recipes.service.api.IExplorerFilterDef;

import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ExplorerFilterDefs {

	private static final IExplorerFilterDef NULL_FILTER = new NullFilterDef();

    public Builder build() {
    	return new Builder();
    }

    public static class Builder {

		private final Set<ITag> includeTags = Sets.newLinkedHashSet();
		private final Set<ITag> excludeTags = Sets.newLinkedHashSet();

    	public Builder includeTag( final ITag inTag) throws IOException {
    		excludeTags.remove(inTag);
    		includeTags.add(inTag);
            return this;
    	}

    	public Builder includeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			includeTag(each);
    		}
            return this;
    	}

    	public Builder includeTags( final Collection<ITag> inTags) throws IOException {
    		for ( ITag each : inTags) {
    			includeTag(each);
    		}
            return this;
    	}

    	public Builder excludeTag( final ITag inTag) throws IOException {
    		includeTags.remove(inTag);
    		excludeTags.add(inTag);
            return this;
    	}

    	public Builder excludeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			excludeTag(each);
    		}
            return this;
    	}

    	public Builder excludeTags( final Collection<ITag> inTags) throws IOException {
    		for ( ITag each : inTags) {
    			excludeTag(each);
    		}
            return this;
    	}

    	public IExplorerFilterDef toFilterDef() {
    		return new DefaultExplorerFilterDef( includeTags, excludeTags);
    	}
    }

    public static IExplorerFilterDef nullFilter() {
    	return NULL_FILTER;
    }

	private static class NullFilterDef implements IExplorerFilterDef {

		@Override
		public Set<ITag> getIncludeTags() {
			return Collections.emptySet();
		}

		@Override
		public Set<ITag> getExcludeTags() {
			return Collections.emptySet();
		}

	}
}