/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

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

import uk.co.recipes.Recipe;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IRecipePersistence;

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
public class EsRecipeFactory implements IRecipePersistence {

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchRecipesUrl")
	String itemIndexUrl;

	@Inject
	ObjectMapper mapper;

	@Inject
	EsUtils esUtils;

	@Inject
	EsSequenceFactory sequences;

    @Inject
    IEventService eventService;


	public Optional<IRecipe> get( final String inName) throws IOException {
		try {
			return Optional.fromNullable( getById( toId(inName) ) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public IRecipe getById( String inId) throws IOException {
		return mapper.readValue( mapper.readTree( new URL( itemIndexUrl + "/" + inId) ).path("_source"), Recipe.class);
	}

	public Optional<IRecipe> getById( long inId) throws IOException {
		if ( inId < Recipe.BASE_ID) {  // Just in case...
			return Optional.absent();
		}

		return esUtils.findOneByIdAndType( itemIndexUrl, inId, IRecipe.class, Recipe.class);
	}

	public IRecipe put( final IRecipe inRecipe, String inId) throws IOException {
		final HttpPost req = new HttpPost( itemIndexUrl + "/" + inId);

		try {
			inRecipe.setId( sequences.getSeqnoForType("recipes_seqno") + Recipe.BASE_ID);

			req.setEntity( new StringEntity( mapper.writeValueAsString(inRecipe) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), isOneOf(201, 200));
			EntityUtils.consume( resp.getEntity() );

			eventService.addRecipe(inRecipe);
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inRecipe;
	}

	public String toStringId( final IRecipe inRecipe) throws IOException {
		return toId( inRecipe.getTitle() );
	}

	public static String toId( final String inCanonicalName) throws IOException {
		return inCanonicalName.toLowerCase().replace( ' ', '_');
	}

    // FIXME - pretty lame!
    public Collection<Recipe> listAll() throws JsonParseException, JsonMappingException, IOException {
        return esUtils.listAll( itemIndexUrl, Recipe.class);
    }

    // FIXME - pretty lame!
    public int countAll() throws IOException {
        return esUtils.countAll( itemIndexUrl, Recipe.class);
    }

	public void deleteAll() throws IOException {
		final HttpResponse resp = httpClient.execute( new HttpDelete(itemIndexUrl) );
		EntityUtils.consume( resp.getEntity() );
	}

	/**
	 * @param items
	 * @return
	 * @throws IOException 
	 */
	public List<IRecipe> getAll( final List<Long> inIds) throws IOException {
		final List<IRecipe> results = Lists.newArrayList();

		for ( final Long eachId : inIds) {
			Optional<IRecipe> oR = getById(eachId);

			if (oR.isPresent()) {  // Shouldn't happen in Production!!
				results.add( oR.get() );
			}
		}

		return results;
	}

    @Override
    public IRecipe getOrCreate(String inCanonicalName, Supplier<IRecipe> inCreator) {
        throw new RuntimeException("unimpl");  // FIXME?
    }

    @Override
    public IRecipe getOrCreate(String inCanonicalName, Supplier<IRecipe> inCreator, boolean inMatchAliases) {
        throw new RuntimeException("unimpl");  // FIXME?
    }
}