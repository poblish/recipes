/**
 * 
 */
package uk.co.recipes.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import uk.co.recipes.UserPreferences;
import uk.co.recipes.api.IExplorerFilterItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUserPreferences;
import uk.co.recipes.service.api.IExplorerFilterDef;

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

    public static IExplorerFilterDef forPrefs( final IUserPreferences inPrefs) {
		return new DefaultExplorerFilterDef((Set<IExplorerFilterItem<?>>) inPrefs.getExplorerIncludes(), (Set<IExplorerFilterItem<?>>) inPrefs.getExplorerExcludes());
    }

    public static class Builder {

    	private final UserPreferences tempPrefs = new UserPreferences();

        public Builder includeTag( final ITag inTag) throws IOException {
            tempPrefs.explorerIncludeAdd(inTag);
            return this;
        }

        public Builder includeTag( final ITag inTag, final String inValue) throws IOException {
            tempPrefs.explorerIncludeAdd(inTag, inValue);
            return this;
        }

    	public Builder includeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			includeTag(each);
    		}
            return this;
    	}

        public Builder excludeTag( final ITag inTag) throws IOException {
            tempPrefs.explorerExcludeAdd(inTag);
            return this;
        }

        public Builder excludeTag( final ITag inTag, final String inValue) throws IOException {
            tempPrefs.explorerExcludeAdd(inTag, inValue);
            return this;
        }

    	public Builder excludeTags( final ITag... inTags) throws IOException {
    		for ( ITag each : inTags) {
    			excludeTag(each);
    		}
            return this;
    	}

    	public IExplorerFilterDef toFilterDef() {
    		return new DefaultExplorerFilterDef((Set<IExplorerFilterItem<?>>) tempPrefs.getExplorerIncludes(), (Set<IExplorerFilterItem<?>>) tempPrefs.getExplorerExcludes());
    	}
    }

    public static IExplorerFilterDef nullFilter() {
    	return NULL_FILTER;
    }

	private static class NullFilterDef implements IExplorerFilterDef {

		@Override
		public Set<IExplorerFilterItem<?>> getIncludes() {
			return Collections.emptySet();
		}

		@Override
		public Set<IExplorerFilterItem<?>> getExcludes() {
			return Collections.emptySet();
		}

	}
}