/**
 * 
 */
package uk.co.recipes.persistence;

import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_NAME_GETS;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPES_PUTS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.HttpClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.ForkDetails;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IRecipePersistence;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsRecipeFactory implements IRecipePersistence {

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger( EsRecipeFactory.class );

	@Inject Client esClient;
	@Inject HttpClient httpClient;
	@Inject EsUtils esUtils;
	@Inject EsSequenceFactory sequences;
	@Inject ObjectMapper mapper;

	@Inject
	@Named("elasticSearchRecipesUrl")
	String itemIndexUrl;

	@Inject MetricRegistry metrics;
	@Inject IEventService eventService;


	public Optional<IRecipe> get( final String inName) throws IOException {
		try {
			return Optional.fromNullable( getByName(inName) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public IRecipe getByName( String inName) throws IOException {
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

		return Optional.fromNullable((IRecipe) mapper.readValue( esClient.prepareGet( "recipe", "recipes", String.valueOf(inId)).execute().actionGet().getSourceAsString(), Recipe.class));
	}

	public IRecipe put( final IRecipe inRecipe, String inId_Unused) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_RECIPES_PUTS).time();

	    final long newId = sequences.getSeqnoForType("recipes_seqno") + Recipe.BASE_ID;

		try {
			inRecipe.setId(newId);

			esClient.prepareIndex( "recipe", "recipes", String.valueOf(newId))/*.setCreate(true) */.setSource( mapper.writeValueAsString(inRecipe) ).execute().actionGet();

			eventService.addRecipe(inRecipe);
		}
        finally {
            timerCtxt.stop();
        }

		return inRecipe;
	}

	@Override
	public void addIngredients( final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException {

		if (!inRecipe.addIngredients(inIngredients) ) {
        	throw new RuntimeException("Ingredient(s) could not be added");
        }

		handledChangedItems( inRecipe, new Runnable() {

			@Override
			public void run() {
				eventService.addRecipeIngredients( inRecipe, inIngredients);
			}
		});
	}

	@Override
	public void removeIngredients( final IRecipe inRecipe, final IIngredient... inIngredients) throws IOException {

		if (!inRecipe.removeIngredients(inIngredients) ) {
        	throw new RuntimeException("Ingredient(s) could not be removed");
        }

		handledChangedItems( inRecipe, new Runnable() {

			@Override
			public void run() {
				eventService.removeRecipeIngredients( inRecipe, inIngredients);
			}
		});
	}

	@Override
	public void removeItems( final IRecipe inRecipe, final ICanonicalItem... inItems) throws IOException {

    	final Collection<IIngredient> ingredientsToRemove = Lists.newArrayList();

    	// We need to convert back Item to Ingredient, so look among all Ingredients to find matches
        for ( IIngredient eachIngr : inRecipe.getIngredients()) {
            for ( ICanonicalItem eachItem : inItems) {
            	if (eachIngr.getItem().equals(eachItem)) {
            		ingredientsToRemove.add(eachIngr);
            		break;
            	}
            }
        }

        if (!inRecipe.removeItems(inItems) ) {
        	throw new RuntimeException("Item(s) could not be removed");
        }

		handledChangedItems( inRecipe, new Runnable() {

			@Override
			public void run() {
				eventService.removeRecipeIngredients( inRecipe, Iterables.toArray( ingredientsToRemove, IIngredient.class));
			}
		});
	}

	private void handledChangedItems( final IRecipe inRecipe, final Runnable inEventServiceCallback) throws IOException {
		// I guess we should wait for this to return...
		esClient.prepareIndex( "recipe", "recipes", String.valueOf( inRecipe.getId() )).setSource( mapper.writeValueAsString(inRecipe) ).execute().actionGet();
		inEventServiceCallback.run();
	}

	public String toStringId( final IRecipe inRecipe) throws IOException {
        return String.valueOf( inRecipe.getId() ); // inRecipe.getTitle().toLowerCase().replace( ' ', '_');
	}

    // FIXME - pretty lame!
    public Collection<Recipe> listAll() throws IOException {
        return esUtils.listAll( itemIndexUrl, Recipe.class);
    }

    public long countAll() throws IOException {
        return esUtils.countAll("recipes");
    }

	public void deleteAll() throws IOException {
		try {
			esClient.admin().indices().prepareDeleteMapping().setIndices("recipe").setType("recipes").execute().actionGet();

            EsUtils.addPartialMatchMappings(esClient);
		}
		catch (TypeMissingException e) {
			// Ignore
		}
		catch (IndexMissingException e) {
			// Ignore
		}
	}

	public void waitUntilRefreshed() {
		esUtils.waitUntilTypesRefreshed("recipes");
	}

	/**
	 * @param items
	 * @return
	 * @throws IOException 
	 */
	public List<IRecipe> getAll( final List<Long> inIds) throws IOException {
		final Timer.Context timerCtxt = metrics.timer("recipes.getAll").time();

		final Collection<String> stringIds = Sets.newHashSet();
		for ( Long each : inIds) {
			stringIds.add( String.valueOf(each) );
		}

		try {
			return esUtils.deserializeRecipeHits( esClient.prepareMultiGet().add( "recipe", "recipes", stringIds).execute().actionGet() );
		}
		finally {
			timerCtxt.close();
		}
	}

    @Override
    public IRecipe getOrCreate(String inCanonicalName, Supplier<IRecipe> inCreator) {
        throw new RuntimeException("unimpl");  // FIXME?
    }

    @Override
    public IRecipe getOrCreate(String inCanonicalName, Supplier<IRecipe> inCreator, boolean inMatchAliases) {
        throw new RuntimeException("unimpl");  // FIXME?
    }

    public IRecipe fork( final IRecipe inOriginalRecipe, final String inNewName) throws IOException {
        return fork( inOriginalRecipe, inNewName, null);
    }

    public IRecipe fork( final IRecipe inOriginalRecipe, final String inNewName, final PreForkChange<IRecipe> inPreChange) throws IOException {
        final IRecipe clone = (IRecipe) inOriginalRecipe.clone();

        final boolean inNameSpecified = ( inNewName != null && !inNewName.isEmpty());
        final String newTitle = inNameSpecified ? inNewName : clone.getTitle() + "-" + UUID.randomUUID();

        clone.setTitle(newTitle);
        clone.setForkDetails( new ForkDetails(inOriginalRecipe) );

        if ( inPreChange != null) {
        	inPreChange.apply(clone);
        }
        return put( clone, /* Unused */ null);
    }

    public void useCopy( final IRecipe inOriginalRecipe, final PreForkChange<IRecipe> inPreChange, final PostForkChange<IRecipe> inPostChange) throws IOException {
        final IRecipe theFork = fork( inOriginalRecipe, null, inPreChange);

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
        eventService.deleteRecipe(inRecipe);
    }

    public void deleteNow( final IRecipe inRecipe) throws IOException {
        esClient.prepareDelete( "recipe", "recipes", String.valueOf( inRecipe.getId() )).execute().actionGet();
        eventService.deleteRecipe(inRecipe);
    }

    public interface PreForkChange<T> {
        void apply( final T recipe);
    }

    public interface PostForkChange<T> {
        void apply( final T inObj) throws IOException;
    }
}