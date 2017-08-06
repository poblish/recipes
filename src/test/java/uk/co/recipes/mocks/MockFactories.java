package uk.co.recipes.mocks;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.mockito.Mockito;
import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO
 *
 * @author andrewregan
 */
public class MockFactories {

    private static final Map<String,ICanonicalItem> ITEMS = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static EsItemFactory inMemoryItemFactory() {
        try {
            final EsItemFactory iff = mock(EsItemFactory.class);

            when(iff.get(anyString())).thenAnswer(call -> {
                final String name = (String) call.getArguments()[0];
                return Optional.of(cachedGet(name));
            });

            when(iff.getOrCreate(anyString(), Mockito.any(Supplier.class))).thenAnswer(call -> {
                final String name = (String) call.getArguments()[0];
                final ICanonicalItem itemOrNull = ITEMS.get(name);
                if (itemOrNull != null) {
                    return itemOrNull;
                }

                final Supplier<ICanonicalItem> getter = (Supplier<ICanonicalItem>) call.getArguments()[1];

                // System.out.println("Creating '" + inCanonicalName + "' ...");

                return getter.get();
                // return put( getter.get(), toId(name));
            });

            return iff;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ICanonicalItem cachedGet(final String name) {
        final ICanonicalItem cached = ITEMS.get(name);
        if (cached != null) {
            return cached;
        }

        final ICanonicalItem item = new CanonicalItem(name);
        item.setId(Math.abs(name.hashCode()));  // Yuk!
        ITEMS.put(name, item);
        return item;
    }
}