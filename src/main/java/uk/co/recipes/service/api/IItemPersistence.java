package uk.co.recipes.service.api;

import java.io.IOException;

import uk.co.recipes.api.ICanonicalItem;

import com.google.common.base.Optional;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IItemPersistence extends IPersistenceAPI<ICanonicalItem> {

	Optional<ICanonicalItem> findBestMatchByName( final String[] inNames) throws IOException;
}