/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.elasticsearch.client.Client;

import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSequenceFactory {

	private final static String INDEX = "sequence";
	private final static String TYPE = "sequence";

	@Inject
	Client esClient;

	/**
	 * Inspired by http://blogs.perl.org/users/clinton_gormley/2011/10/elasticsearchsequence---a-blazing-fast-ticket-server.html
	 * @param inIdForEntityType the 'type' of sequence
	 * @return a unique seqno for this 'inIdForEntityType' value
	 */
	public long getSeqnoForType( final String inIdForEntityType) {
		try {
			return esClient.prepareIndex( INDEX, TYPE).setId(inIdForEntityType).setSource("{}").execute().get().getVersion();
		}
		catch (InterruptedException e) {
			throw Throwables.propagate(e);
		}
		catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}

	public void deleteAll() throws IOException {
		esClient.admin().indices().prepareDeleteMapping().setIndices(INDEX).setType(TYPE).execute().actionGet();
	}
}