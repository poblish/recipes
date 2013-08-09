/**
 * 
 */
package uk.co.recipes.persistence;

import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static uk.co.recipes.metrics.MetricNames.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IRecipePersistence;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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
public class EsRecipeFactory implements IRecipePersistence {

//	private final static Logger LOG = LoggerFactory.getLogger( EsRecipeFactory.class );

	@Inject
	Client esClient;

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
	MetricRegistry metrics;

    @Inject
    IEventService eventService;


	public Optional<IRecipe> get( final String inName) throws IOException {
		try {
			return Optional.fromNullable( getById(inName) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public IRecipe getById( String inName) throws IOException {
	    final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_NAME_GETS).time();

        try {
            final SearchHit[] hits = esClient.prepareSearch("recipe").setTypes("recipes").setQuery( fieldQuery( "title", inName) ).setSize(1).execute().get().getHits().hits();
            if ( hits.length < 1) {
                return null;
            }
            return mapper.readValue( hits[0].getSourceAsString(), Recipe.class);
        }
        catch (InterruptedException e) {
            Throwables.propagate(e);
        }
        catch (ExecutionException e) {
            Throwables.propagate(e);
        }
        finally {
            timerCtxt.stop();
        }

        return null;
	}

	public Optional<IRecipe> getById( long inId) throws IOException {
		if ( inId < Recipe.BASE_ID) {  // Just in case...
			return Optional.absent();
		}

		return esUtils.findOneByIdAndType( itemIndexUrl, inId, IRecipe.class, Recipe.class);
	}

	public IRecipe put( final IRecipe inRecipe, String inId_Unused) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_PUTS).time();

	    final long newId = sequences.getSeqnoForType("recipes_seqno") + Recipe.BASE_ID;

	    final HttpPost req = new HttpPost( itemIndexUrl + "/" + newId);

		try {
			inRecipe.setId(newId);

			req.setEntity( new StringEntity( mapper.writeValueAsString(inRecipe) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), isOneOf(201, 200));
			EntityUtils.consume( resp.getEntity() );

	        metrics.counter(COUNTER_RECIPES_PUTS).inc();

			eventService.addRecipe(inRecipe);
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}
        finally {
            timerCtxt.stop();
        }

		return inRecipe;
	}

	public String toStringId( final IRecipe inRecipe) throws IOException {
        return String.valueOf( inRecipe.getId() ); // inRecipe.getTitle().toLowerCase().replace( ' ', '_');
	}

    // FIXME - pretty lame!
    public Collection<Recipe> listAll() throws IOException {
        return esUtils.listAll( itemIndexUrl, Recipe.class);
    }

    public long countAll() throws IOException {
        return esUtils.countAll(itemIndexUrl);
    }

	public void deleteAll() throws IOException {
		try {
			esClient.admin().indices().prepareDeleteMapping().setIndices("recipe").setType("recipes").execute().actionGet();
		}
		catch (TypeMissingException e) {
			// Ignore
		}
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

    public IRecipe fork( final IRecipe inModifiedRecipe) throws IOException {
        return fork( inModifiedRecipe, null);
    }

    public IRecipe fork( final IRecipe inModifiedRecipe, final PreForkChange<IRecipe> inPreChange) throws IOException {
        final String newId = toStringId(inModifiedRecipe) + "_" + System.nanoTime();
        final IRecipe clone = (IRecipe) inModifiedRecipe.clone();
        if ( inPreChange != null) {
        	inPreChange.apply(clone);
        }
        return put( clone, newId);
    }

    public void useCopy( final IRecipe inModifiedRecipe, final PreForkChange<IRecipe> inPreChange, final PostForkChange<IRecipe> inPostChange) throws IOException {
        final IRecipe theFork = fork( inModifiedRecipe, inPreChange);

        try {
            if ( inPostChange != null) {
            	inPostChange.apply(theFork);
            }
        }
        finally {
            delete(theFork);
        }
    }

    public void delete( final IRecipe inRecipe) throws IOException {
        esClient.prepareDelete( "recipe", "recipes", String.valueOf( inRecipe.getId() )).execute();  // Don't need to wait for this
    }

    public void deleteNow( final IRecipe inRecipe) throws IOException {
        esClient.prepareDelete( "recipe", "recipes", String.valueOf( inRecipe.getId() )).execute().actionGet();
    }

    public interface PreForkChange<T> {
        void apply( final T recipe);
    }

    public interface PostForkChange<T> {
        void apply( final T inObj) throws IOException;
    }
}