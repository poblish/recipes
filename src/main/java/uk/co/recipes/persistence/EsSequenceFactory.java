/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.util.EntityUtils;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsSequenceFactory {

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchSequenceUrl")
	String itemIndexUrl;


	public void deleteAll() throws IOException {
		EntityUtils.consume( httpClient.execute( new HttpDelete(itemIndexUrl) ).getEntity() );
	}
}