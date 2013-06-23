/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ItemsLoader {

	private final static Optional<ICanonicalItem> MISSING = Optional.absent();

	public static void load() throws IOException {
		for ( Object each : new Yaml().loadAll( Files.toString( new File("src/test/resources/inputs.yaml"), Charset.forName("utf-8")))) {

			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) each;

			final String name = (String) map.get("canonicalName");
			final String parentName = (String) map.get("parent");
			final Optional<ICanonicalItem> parentCI = ( parentName != null) ? CanonicalItemFactory.get(parentName) : MISSING;

			CanonicalItemFactory.getOrCreate( name, new Supplier<ICanonicalItem>() {

				@Override
				public ICanonicalItem get() {
					final ICanonicalItem newItem = new CanonicalItem( name, parentCI);

					final Object tagsObj = map.get("tags");
					if ( tagsObj instanceof String) {
						newItem.addTag( CommonTags.valueOf((String) tagsObj) );
					}
					else if ( tagsObj instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String,Object> m = (Map<String,Object>) tagsObj;
						for ( String each : m.keySet()) {
							if (each.startsWith("-")) {
								newItem.addTag( CommonTags.valueOf( each.substring(1)), Boolean.FALSE);
							}
							else {
								newItem.addTag( CommonTags.valueOf(each) );
							}
						}
					}

					return newItem;
				}});
		}
	}
}
