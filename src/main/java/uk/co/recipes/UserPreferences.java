/**
 * 
 */
package uk.co.recipes;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IExplorerFilterItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUserPreferences;
import uk.co.recipes.tags.TagUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class UserPreferences implements IUserPreferences {

	private Set<IExplorerFilterItem<?>> includes = Sets.newLinkedHashSet();
	private Set<IExplorerFilterItem<?>> excludes = Sets.newLinkedHashSet();

	@Override
	public Collection<IExplorerFilterItem<?>> getExplorerIncludes() {
		return includes;
	}

	@Override
	public Collection<IExplorerFilterItem<?>> getExplorerExcludes() {
		return excludes;
	}

	@Override
	public boolean explorerIncludeAdd( ITag inTag) {
		return explorerIncludeAdd( inTag, null);
	}

    @Override
    public boolean explorerIncludeAdd( ITag inTag, String inValue) {
        explorerExcludeRemove(inTag);
        return includes.add( new TagFilterItem( inTag, inValue) );
    }

	@Override
	public boolean explorerIncludeAdd( ICanonicalItem item) {
		explorerExcludeRemove(item);
		return includes.add( new ItemFilterItem( item.getCanonicalName() ) );
	}

	@Override
	public boolean explorerExcludeAdd( ITag inTag) {
        return explorerExcludeAdd( inTag, null);
	}

    @Override
    public boolean explorerExcludeAdd( ITag inTag, String inValue) {
        explorerIncludeRemove(inTag);
        return excludes.add( new TagFilterItem( inTag, inValue) );
    }

	@Override
	public boolean explorerExcludeAdd( ICanonicalItem item) {
		explorerIncludeRemove(item);
		return excludes.add( new ItemFilterItem( item.getCanonicalName() ) );
	}

	@Override
	public boolean explorerIncludeRemove( ITag inTag) {
		return explorerIncludeRemove( inTag, null);
	}

    @Override
    public boolean explorerIncludeRemove( ITag inTag, String inValue) {
        return includes.remove( new TagFilterItem( inTag, inValue) );
    }

	@Override
	public boolean explorerIncludeRemove( ICanonicalItem item) {
		return includes.remove( new ItemFilterItem( item.getCanonicalName() ) );
	}

    @Override
    public boolean explorerExcludeRemove( ITag inTag) {
        return explorerExcludeRemove( inTag, null);
    }

    @Override
    public boolean explorerExcludeRemove( ITag inTag, String inValue) {
        return excludes.remove( new TagFilterItem( inTag, inValue) );
    }

	@Override
	public boolean explorerExcludeRemove( ICanonicalItem item) {
		return excludes.remove( new ItemFilterItem( item.getCanonicalName() ) );
	}

	@Override
	public boolean explorerClearAll() {
		boolean gotSome = !includes.isEmpty() || !excludes.isEmpty();
		includes.clear();
		excludes.clear();
		return gotSome;
	}

	public String toString() {
		return Objects.toStringHelper(this)
						.add("includes", getExplorerIncludes())
						.add("excludes", getExplorerExcludes())
						.toString();
	}


    private static class TagFilterItem implements IExplorerFilterItem<ITag> {

        private ITag tag;
        private String value;

        public TagFilterItem( final ITag tag, final String value) {
            this.tag = tag;
            this.value = Strings.emptyToNull(value);
        }

		@SuppressWarnings("unused")  // Used by Jackson!
		public String getFilter() {
			if ( value != null) {
			    return "Tag|" + tag.toString() + "|" + value;
			}
			return "Tag|" + tag.toString();
		}

		@JsonIgnore
		@Override
		public ITag getEntity() {
			return tag;
		}

		@JsonIgnore
		@Override
		public Optional<String> getValue() {
			return Optional.fromNullable(value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode( tag, value);
		}

		@Override
		public boolean equals( Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TagFilterItem)) {
				return false;
			}
			final TagFilterItem other = (TagFilterItem) obj;
			return Objects.equal( tag, other.tag) && Objects.equal( value, other.value);
		}

		public String toString() {
			return Objects.toStringHelper(this).omitNullValues().add( "tag", tag).add( "value", value).toString();
		}
    }

    private static class ItemFilterItem implements IExplorerFilterItem<String> {

		private String canonicalName;

		public ItemFilterItem( final String inName) {
			this.canonicalName = inName;
		}

		@SuppressWarnings("unused")  // Used by Jackson!
		public String getFilter() {
			return "Item|" + canonicalName;
		}

		@JsonIgnore
		@Override
		public String getEntity() {
			return canonicalName;
		}

		@JsonIgnore
		@Override
		public Optional<String> getValue() {
			return Optional.absent();
		}

		@Override
		public int hashCode() {
			return canonicalName.hashCode();
		}

		@Override
		public boolean equals( Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ItemFilterItem)) {
				return false;
			}
			return Objects.equal( canonicalName, ((ItemFilterItem) obj).canonicalName);
		}

		public String toString() {
			return Objects.toStringHelper(this).add( "name", canonicalName).toString();
		}
    }

    public static JsonDeserializer<IExplorerFilterItem<?>> explorerFilterItemsDeser() {
    	return new JsonDeserializer<IExplorerFilterItem<?>>() {

			@Override
			public IExplorerFilterItem<?> deserialize( JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                jp.nextValue();  // Ignore field name!
                final String stringRepr = jp.getText();
                jp.nextValue();  // Must skip to next now, to avoid confusing Parser!

				if (stringRepr.startsWith("Tag|")) {
				    final int valPipeIdx = stringRepr.indexOf( '|', 4);
				    if ( valPipeIdx < 0) {  // No value section
				        return new TagFilterItem( TagUtils.forName( stringRepr.substring(4) ), null);
				    }
				    else {  // Got value!
                        return new TagFilterItem( TagUtils.forName( stringRepr.substring( 4, valPipeIdx) ), stringRepr.substring( valPipeIdx + 1));
				    }
				}
				else if (stringRepr.startsWith("Item|")) {
					return new ItemFilterItem( stringRepr.substring(5) );
				}
				return null;
			}};
    }
}