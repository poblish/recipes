/**
 * 
 */
package uk.co.recipes.service.api;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IPersistenceAPI<T> {

    T put( final T inItem, String inId) throws IOException;

    Optional<T> getById( long inId) throws IOException;
    Optional<T> get( final String name) throws IOException;
    T getByName( String name) throws IOException;

    T getOrCreate( final String inCanonicalName, final Supplier<T> inCreator);
    T getOrCreate( final String inCanonicalName, final Supplier<T> inCreator, final boolean inMatchAliases);
    List<T> getAll( final List<Long> inIds) throws IOException;

    String toStringId( final T obj) throws IOException;

    long countAll() throws IOException;

    void delete( final T obj) throws IOException;
    void deleteNow( final T obj) throws IOException;
    void deleteAll() throws IOException;

    void waitUntilRefreshed() throws InterruptedException;
}