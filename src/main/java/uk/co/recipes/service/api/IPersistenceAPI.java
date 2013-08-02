/**
 * 
 */
package uk.co.recipes.service.api;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import java.util.List;
import java.io.IOException;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IPersistenceAPI<T> {

    T put( final T inItem, String inId) throws IOException;

    T getOrCreate( final String inCanonicalName, final Supplier<T> inCreator);
    T getOrCreate( final String inCanonicalName, final Supplier<T> inCreator, final boolean inMatchAliases);
    Optional<T> get( final String inCanonicalName) throws IOException;
    T getById( String inId) throws IOException;
    List<T> getAll( final List<Long> inIds) throws IOException;

    String toStringId( final T obj) throws IOException;

    int countAll() throws IOException;

    void deleteAll() throws IOException;
}