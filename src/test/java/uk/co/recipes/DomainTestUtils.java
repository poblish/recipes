package uk.co.recipes;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.service.api.ISearchResult;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DomainTestUtils {

    public static List<String> names(final List<ISearchResult<?>> searchResults) {
        return searchResults.stream().map(ISearchResult::getDisplayName).collect(toList());
    }

    public static List<String> canonicalNames(final List<ICanonicalItem> items) {
        return items.stream().map(ICanonicalItem::getCanonicalName).collect(toList());
    }
}
