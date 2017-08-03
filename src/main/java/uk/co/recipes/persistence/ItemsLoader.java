/**
 *
 */
package uk.co.recipes.persistence;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Quantity;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.tags.TagUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import static uk.co.recipes.metrics.MetricNames.TIMER_LOAD_ITEM_PROCESSITEM;

/**
 * TODO
 *
 * @author andrewregan
 */
public class ItemsLoader {

    @Inject
    MetricRegistry metrics;
    @Inject
    EsItemFactory itemFactory;
    @Inject
    IngredientParser parser;
    @Inject
    ConfigurationProvider config;

    private static final Logger LOG = LoggerFactory.getLogger(ItemsLoader.class);
    private static final Optional<ICanonicalItem> MISSING = Optional.absent();

    @Inject
    public ItemsLoader() {
        // For Dagger
    }

    public void load() throws IOException {
        final List<Map<String,Object>> entriesToDefer = Lists.newArrayList();

        final Set<String> topLevelNamesCache = Sets.newHashSet();
        final File path = config.getProperty("loader.items.path", File.class);

        for (Object each : new Yaml().loadAll(Files.toString(path, Charset.forName("utf-8")))) {

            @SuppressWarnings("unchecked") final Map<String,Object> map = (Map<String,Object>) each;

            final String parentName = (String) map.get("parent");
            final Optional<ICanonicalItem> parentCI = (parentName != null) ? itemFactory.get(parentName) : MISSING;

            if (parentName != null && !parentCI.isPresent()) {
                // Parent doesn't exist yet, defer...
                entriesToDefer.add(map);
                continue;
            }

            if (!processItem(map, parentCI, topLevelNamesCache)) {
                // Most probably because of missing constituent
                entriesToDefer.add(map);
            }
        }

        // Note that we don't support *recursive* deferring, e.g. if hierarchy is specified backwards
        for (Map<String,Object> eachDeferred : entriesToDefer) {
            final String parentName = (String) eachDeferred.get("parent");
            final Optional<ICanonicalItem> parentCI = (parentName != null) ? itemFactory.get(parentName) : MISSING;

            if (parentName != null && !parentCI.isPresent()) {
                // Parent doesn't exist yet, defer
                throw new RuntimeException("Missing parent item '" + parentName + "' for '" + eachDeferred.get("canonicalName") + "'");
            }

            if (!processItem(eachDeferred, parentCI, topLevelNamesCache)) {
                throw new RuntimeException("Deferral failed for " + eachDeferred);  // FIXME!
            }
        }
    }

    private boolean processItem(final Map<String,Object> inMap, final Optional<ICanonicalItem> inParent, final Set<String> inTopLevelNamesCache) {
        try (Context ctxt = metrics.timer(TIMER_LOAD_ITEM_PROCESSITEM).time()) {
            return timedProcessItem(inMap, inParent, inTopLevelNamesCache);
        }
    }

    private boolean timedProcessItem(final Map<String,Object> inMap, final Optional<ICanonicalItem> inParent, final Set<String> inTopLevelNamesCache) {
        final String name = (String) inMap.get("canonicalName");

        if (inTopLevelNamesCache.contains(name)) {
            throw new RuntimeException("Duplicate Name: " + name);
        }

        //////////////////////////////////////////////////////////////////////////////

        final List<ICanonicalItem> validConstitutents = Lists.newArrayList();

        for (String eachConstitName : yamlObjectToStrings(inMap.get("contains"))) {
            try {
                final Optional<ICanonicalItem> constituent = itemFactory.get(eachConstitName);
                if (!constituent.isPresent()) {
                    // Missing constituent 'eachConstitName' - defer and try again on the second pass
                    return false;
                }

                validConstitutents.add(constituent.get());
            } catch (IOException e) {
                Throwables.propagate(e);  // Yuk!
            }
        }

        inTopLevelNamesCache.add(name);

        //////////////////////////////////////////////////////////////////////////////

        itemFactory.getOrCreate(name, new Supplier<ICanonicalItem>() {

            @SuppressWarnings("unchecked")
            @Override
            public ICanonicalItem get() {
                final ICanonicalItem newItem = new CanonicalItem(name, inParent);

                for (Object eachTag : yamlObjectToStrings(inMap.get("tags"))) {
                    if (eachTag instanceof Map) {
                        for (Entry<Object,Object> eachEntry : ((Map<Object,Object>) eachTag).entrySet()) {
                            processTagValue(newItem, (String) eachEntry.getKey(), String.valueOf(eachEntry.getValue()));
                        }
                    } else {
                        processTagValue(newItem, (String) eachTag, Boolean.TRUE);
                    }
                }

                if (inParent.isPresent() && ((CanonicalItem) newItem).hasOverlappingTags()) {
                    LOG.warn("Overlapping Tags for: " + newItem);
                }

                if (((CanonicalItem) newItem).hasUnecessaryCancelTags()) {
                    LOG.warn("Unnecessary Cancel tags (" + ((CanonicalItem) newItem).getCancelTags() + ") among: " + newItem);
                }

                ///////////////////////////////////////////////////////////////////////

                for (String eachAlias : yamlObjectToStrings(inMap.get("aliases"))) {
                    ((CanonicalItem) newItem).aliases.add(eachAlias);
                }

                if (!validConstitutents.isEmpty()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Adding " + validConstitutents + " to " + newItem);
                    }
                    ((CanonicalItem) newItem).constituents.addAll(validConstitutents);
                }

                final String baseAmt = (String) inMap.get("baseAmt");
                if (baseAmt != null) {
                    final Optional<Quantity> parsedQ = parser.parseQuantity(baseAmt);
                    if (parsedQ.isPresent()) {
                        ((CanonicalItem) newItem).setBaseAmount(parsedQ.get());
                    }
                }

                return newItem;
            }
        });

        return true;  // OK
    }

    private void processTagValue(final ICanonicalItem ioItem, final String inTagName, final Serializable inValue) {
        if (inTagName.startsWith("-")) {
            ((CanonicalItem) ioItem).addCancelTag(TagUtils.forName(inTagName.substring(1)));
        } else {
            ioItem.addTag(TagUtils.forName(inTagName), inValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> yamlObjectToStrings(final Object inYamlObj) {
        if (inYamlObj == null) {
            return Collections.emptyList();
        }

        if (inYamlObj instanceof String) {
            return Lists.newArrayList((String) inYamlObj);
        } else if (inYamlObj instanceof Map) {
            return ((Map<String,Object>) inYamlObj).keySet();
        } else {
            throw new RuntimeException("Unexpected type: " + inYamlObj.getClass());
        }
    }
}
