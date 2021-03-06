/**
 *
 */
package uk.co.recipes.persistence;

import com.google.common.base.Throwables;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.TypeMissingException;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

/**
 * TODO
 *
 * @author andrewregan
 */
public class EsSequenceFactory {

    private static final String INDEX = "sequence";
    private static final String TYPE = "sequence";

    @Inject
    Client esClient;
    @Inject
    EsUtils esUtils;

    @Inject
    public EsSequenceFactory() {
        // For Dagger
    }

    /**
     * Inspired by http://blogs.perl.org/users/clinton_gormley/2011/10/elasticsearchsequence---a-blazing-fast-ticket-server.html
     *
     * @param inIdForEntityType the 'type' of sequence
     * @return a unique seqno for this 'inIdForEntityType' value
     */
    public long getSeqnoForType(final String inIdForEntityType) {
        try {
            return esClient.prepareIndex(INDEX, TYPE).setId(inIdForEntityType).setSource("{}").execute().get().getVersion();
        } catch (InterruptedException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    public void deleteAll() {
        try {
            esUtils.deleteAllByType(INDEX, TYPE);
        } catch (TypeMissingException e) {
            // Ignore
        }
    }
}