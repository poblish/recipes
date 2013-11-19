/**
 * 
 */
package uk.co.recipes.persistence;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.HttpClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.User;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.IUserAuth;
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

	@Inject Client esClient;
	@Inject HttpClient httpClient;
	@Inject ObjectMapper mapper;
	@Inject EsUtils esUtils;
	@Inject EsSequenceFactory sequences;

	@Inject
	@Named("elasticSearchUsersUrl")
	String usersIndexUrl;


	public IUser put( final IUser inUser, String inId) throws IOException {
		inUser.setId( sequences.getSeqnoForType("users_seqno") );
		esClient.prepareIndex( "recipe", "users", inId).setSource( mapper.writeValueAsString(inUser) ).execute().actionGet();
		return inUser;
	}

	public void update( final IUser inUser) throws IOException {
		esClient.prepareIndex( "recipe", "users", toStringId(inUser) ).setSource( mapper.writeValueAsString(inUser) ).execute().actionGet();
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
        return esUtils.countAll("users");
    }

	public void waitUntilRefreshed() {
		esUtils.waitUntilTypesRefreshed("users");
	}

	@Override
	public Optional<IUser> findWithAuth( final IUserAuth inAuth) throws IOException {
        try {
			final SearchHit[] hits = esClient.prepareSearch("recipe").setTypes("users").setQuery( boolQuery().must( fieldQuery( "authId", inAuth.getAuthId() )).must( fieldQuery( "authProvider", inAuth.getAuthProvider()) ) ).setSize(2).execute().get().getHits().hits();
			if ( hits.length > 1) {
				throw new RuntimeException("Too many matches for " + inAuth);
			}

			if ( hits.length == 0) {
				return Optional.absent();
			}

            return Optional.of((IUser) mapper.readValue( hits[0].getSourceRef().toBytes(), User.class) );
		}
        catch (InterruptedException e) {
        	throw Throwables.propagate(e);
		}
        catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}

	// FIXME, perhaps
	@Override
	public IUser adminUser() {
		return getOrCreate( "Admin", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "admin", "Admin");
			}
		} );
	}
}