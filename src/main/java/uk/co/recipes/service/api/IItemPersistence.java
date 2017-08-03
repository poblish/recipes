package uk.co.recipes.service.api;

import com.google.common.base.Optional;
import uk.co.recipes.api.ICanonicalItem;

import java.io.IOException;

/**
 * TODO
 *
 * @author andrewr
 */
public interface IItemPersistence extends IPersistenceAPI<ICanonicalItem> {

    Optional<ICanonicalItem> findBestMatchByName(final String[] inNames) throws IOException;
}