/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.yaml.snakeyaml.Yaml;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.tags.TagUtils;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ItemsLoader {

	@Inject
	CanonicalItemFactory itemFactory;

	private final static Optional<ICanonicalItem> MISSING = Optional.absent();

	public void load() throws IOException {
		for ( Object each : new Yaml().loadAll( Files.toString( new File("src/test/resources/inputs.yaml"), Charset.forName("utf-8")))) {

			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) each;

			final String name = (String) map.get("canonicalName");
			final String parentName = (String) map.get("parent");
			final Optional<ICanonicalItem> parentCI = ( parentName != null) ? itemFactory.get(parentName) : MISSING;

			itemFactory.getOrCreate( name, new Supplier<ICanonicalItem>() {

				@Override
				public ICanonicalItem get() {
					final ICanonicalItem newItem = new CanonicalItem( name, parentCI);

					for ( String each : yamlObjectToStrings( map.get("tags") )) {
						if (each.startsWith("-")) {
							newItem.addTag( TagUtils.forName( each.substring(1)), Boolean.FALSE);
						}
						else {
							newItem.addTag( TagUtils.forName(each) );
						}
					}

					for ( String each : yamlObjectToStrings( map.get("aliases") )) {
						((CanonicalItem) newItem).aliases.add(each);
					}

					return newItem;
				}});
		}
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> yamlObjectToStrings( final Object inYamlObj) {
		if ( inYamlObj == null) {
			return Collections.emptyList();
		}

		if ( inYamlObj instanceof String) {
			return Lists.newArrayList((String) inYamlObj);
		}
		else if ( inYamlObj instanceof Map) {
			return ((Map<String,Object>) inYamlObj).keySet();
		}
		else {
			throw new RuntimeException("Unexpected type: " + inYamlObj.getClass());
		}
	}
}
