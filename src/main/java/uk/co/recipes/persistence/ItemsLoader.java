/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	EsItemFactory itemFactory;

	private static final Logger LOG = LoggerFactory.getLogger( ItemsLoader.class );
	private static final Optional<ICanonicalItem> MISSING = Optional.absent();

	public void load() throws IOException {
	    final List<Map<String,Object>> entriesToDefer = Lists.newArrayList();

		for ( Object each : new Yaml().loadAll( Files.toString( new File("src/test/resources/inputs.yaml"), Charset.forName("utf-8")))) {

			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) each;

			final String parentName = (String) map.get("parent");
			final Optional<ICanonicalItem> parentCI = ( parentName != null) ? itemFactory.get(parentName) : MISSING;
 
			if ( parentName != null && !parentCI.isPresent()) {
                // Parent doesn't exist yet, defer...
			    entriesToDefer.add(map);
			    continue;
            }

			processItem( map, parentCI);
		}

		// Note that we don't support *recursive* deferring, e.g. if hierarchy is specified backwards
		for ( Map<String,Object> eachDeferred : entriesToDefer) {
            final String parentName = (String) eachDeferred.get("parent");
            final Optional<ICanonicalItem> parentCI = ( parentName != null) ? itemFactory.get(parentName) : MISSING;
 
            if ( parentName != null && !parentCI.isPresent()) {
                // Parent doesn't exist yet, defer
                throw new RuntimeException("Missing parent item '" + parentName + "' for '" + eachDeferred.get("canonicalName") + "'");
            }

            processItem( eachDeferred, parentCI);
		}
	}

	private void processItem( final Map<String,Object> inMap, final Optional<ICanonicalItem> inParent) {
        final String name = (String) inMap.get("canonicalName");

        itemFactory.getOrCreate( name, new Supplier<ICanonicalItem>() {

            @Override
            public ICanonicalItem get() {
                final ICanonicalItem newItem = new CanonicalItem( name, inParent);

                for ( String eachTag : yamlObjectToStrings( inMap.get("tags") )) {
                    if (eachTag.startsWith("-")) {
                        newItem.addTag( TagUtils.forName( eachTag.substring(1)), Boolean.FALSE);
                    }
                    else {
                        newItem.addTag( TagUtils.forName(eachTag) );
                    }
                }

                if ( inParent.isPresent() && ((CanonicalItem) newItem).hasOverlappingTags()) {
                    LOG.warn( "Overlapping Tags for: " + newItem);
                }

                for ( String eachAlias : yamlObjectToStrings( inMap.get("aliases") )) {
                    ((CanonicalItem) newItem).aliases.add(eachAlias);
                }

                return newItem;
            }});
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
