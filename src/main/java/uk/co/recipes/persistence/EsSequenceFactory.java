/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;

import javax.inject.Inject;

import org.elasticsearch.client.Client;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSequenceFactory {

	@Inject
	Client esClient;

	public void deleteAll() throws IOException {
		esClient.admin().indices().prepareDeleteMapping().setIndices("sequence").setType("sequence").execute().actionGet();
	}
}