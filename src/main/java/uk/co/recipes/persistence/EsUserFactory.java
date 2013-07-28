/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Client;

import uk.co.recipes.User;
import uk.co.recipes.api.IUser;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsUserFactory {

	@Inject
	Client esClient;

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchUsersUrl")
	String usersIndexUrl;

	@Inject
	ObjectMapper mapper;

	@Inject
	EsUtils esUtils;


	public IUser put( final IUser inUser, String inId) throws IOException {
		final HttpPost req = new HttpPost( usersIndexUrl + "/" + inId);

		try {
			req.setEntity( new StringEntity( mapper.writeValueAsString(inUser) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );

			inUser.setId( esUtils.getSeqnoForType("users_seqno") );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inUser;
	}

	public IUser getById( String inId) throws IOException {
		return mapper.readValue( mapper.readTree( new URL( usersIndexUrl + "/" + inId) ).path("_source"), User.class);
	}

	public String toId( final String inName) throws IOException {
		return inName.toLowerCase().replace( ' ', '_');
	}

	public Optional<IUser> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getById( toId(inCanonicalName) ) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public IUser getOrCreate( final String inCanonicalName, final Supplier<IUser> inCreator) {
		return getOrCreate( inCanonicalName, inCreator, false);
	}

	public IUser getOrCreate( final String inCanonicalName, final Supplier<IUser> inCreator, final boolean inMatchAliases) {
		try {
			final Optional<IUser> got = get(inCanonicalName);
	
			if (got.isPresent()) {
				return got.get();
			}

			System.out.println("Creating '" + inCanonicalName + "' ...");

			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	// FIXME - pretty lame!
	public Collection<User> listAll() throws JsonParseException, JsonMappingException, IOException {
		return esUtils.listAll( usersIndexUrl, User.class);
	}

	public void deleteAll() throws IOException {
		final HttpResponse resp = httpClient.execute( new HttpDelete(usersIndexUrl) );
		EntityUtils.consume( resp.getEntity() );
	}

	public void stopES() {
		esClient.close();
	}
}