/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.TypeMissingException;

import uk.co.recipes.User;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.IUserPersistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsUserFactory implements IUserPersistence {

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

	@Inject
	EsSequenceFactory sequences;


	public IUser put( final IUser inUser, String inId) throws IOException {
		final HttpPost req = new HttpPost( usersIndexUrl + "/" + inId);

		try {
			inUser.setId( sequences.getSeqnoForType("users_seqno") );

			req.setEntity( new StringEntity( mapper.writeValueAsString(inUser) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inUser;
	}

	public IUser getByName( String inId) throws IOException {
		return mapper.readValue( esUtils.parseSource( usersIndexUrl + "/" + inId), User.class);
	}

    public Optional<IUser> getById( long inId) throws IOException {
        return esUtils.findOneByIdAndType( usersIndexUrl, inId, IUser.class, User.class);
    }

	public String toId( final String inName) throws IOException {
		return inName.toLowerCase().replace( ' ', '_');
	}

	public Optional<IUser> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getByName( toId(inCanonicalName) ) );
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

//			System.out.println("Creating '" + inCanonicalName + "' ...");

			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	// FIXME - pretty lame!
	public Collection<User> listAll() throws IOException {
		return esUtils.listAll( usersIndexUrl, User.class);
	}

    @Override
    public void delete( final IUser obj) throws IOException {
        throw new RuntimeException("unimpl");
    }

    @Override
    public void deleteNow( final IUser obj) throws IOException {
        throw new RuntimeException("unimpl");
    }

    @Override
	public void deleteAll() throws IOException {
		try {
			esClient.admin().indices().prepareDeleteMapping().setIndices("recipe").setType("users").execute().actionGet();
		}
		catch (TypeMissingException e) {
			// Ignore
		}
	}

    @Override
    public List<IUser> getAll( final List<Long> inIds) throws IOException {
        final List<IUser> results = Lists.newArrayList();

        for ( final Long eachId : inIds) {
            Optional<IUser> oI = getById(eachId);

            if (oI.isPresent()) {  // Shouldn't happen in Production!!
                results.add( oI.get() );
            }
        }

        return results;
    }

    @Override
    public String toStringId( final IUser obj) throws IOException {
        return toId( obj.getUserName() );
    }

    @Override
    public long countAll() throws IOException {
        return esUtils.countAll(usersIndexUrl);
    }
}