/**
 * 
 */
package uk.co.recipes.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static uk.co.recipes.metrics.MetricNames.*;

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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IItemPersistence;

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
public class EsItemFactory implements IItemPersistence {

	@Inject
	Client esClient;

	@Inject
	HttpClient httpClient;

	@Inject
	@Named("elasticSearchItemsUrl")
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


	public ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_PUTS).time();

		final HttpPost req = new HttpPost( itemIndexUrl + "/" + inId); // URLEncoder.encode( inId, "UTF-8"));
		HttpResponse resp = null;

		try {
			inItem.setId( sequences.getSeqnoForType("items_seqno") );

			req.setEntity( new StringEntity( mapper.writeValueAsString(inItem) ) );

			resp = httpClient.execute(req);
			if ( resp.getStatusLine().getStatusCode() != 201) {
				throw new RuntimeException("Failure for '" + inId + "', code = " + resp.getStatusLine().getStatusCode());
			}

			eventService.addItem(inItem);
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}
        finally {
			EntityUtils.consume( resp.getEntity() );
            timerCtxt.stop();
        }

		return inItem;
	}

	public ICanonicalItem getByName( String inId) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_NAME_GETS).time();

		try {
		    return mapper.readValue( esUtils.parseSource( itemIndexUrl + "/" + inId), CanonicalItem.class);
		}
        finally {
            timerCtxt.stop();
        }
	}

    public Optional<ICanonicalItem> getById( long inId) throws IOException {
        if ( inId >= Recipe.BASE_ID) {  // Just in case...
            return Optional.absent();
        }

        final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_ID_GETS).time();

        try {
            return esUtils.findOneByIdAndType( itemIndexUrl, inId, ICanonicalItem.class, CanonicalItem.class);
        }
        finally {
            timerCtxt.stop();
        }
    }

    public String toStringId( final ICanonicalItem inItem) {
        return toId( inItem.getCanonicalName() );
    }

	public static String toId( final String inCanonicalName) {
		checkArgument( !inCanonicalName.contains(","), "Name should not contain comma: '" + inCanonicalName + "'");
		return inCanonicalName.toLowerCase().replace( ' ', '_');
	}

	public Optional<ICanonicalItem> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getByName( toId(inCanonicalName) ) );
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return Optional.absent();
	}

	public ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator) {
		return getOrCreate( inCanonicalName, inCreator, false);
	}

	public ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator, final boolean inMatchAliases) {
		try {
			final Optional<ICanonicalItem> got = get(inCanonicalName);
	
			if (got.isPresent()) {
				return got.get();
			}

			if (inMatchAliases) {
				try {
					// http://www.elasticsearch.org/guide/reference/query-dsl/match-query/
					final SearchResponse resp = esClient.prepareSearch("recipe").setTypes("items").setQuery( matchPhraseQuery( "aliases", inCanonicalName.toLowerCase()) ).addSort( "_score", DESC).execute().actionGet();
					final SearchHit[] hits = resp.getHits().hits();

					if ( /* Yes, want only one great match */ hits.length == 1) {
						final ICanonicalItem mappedAlias = mapper.readValue( hits[0].getSourceAsString(), CanonicalItem.class);
						if ( mappedAlias != null) {
//							System.out.println("Successfully mapped Alias '" + inCanonicalName + "' => " + mappedAlias);
							return mappedAlias;
						}
					}
				}
				catch (IOException e) { /* e.printStackTrace(); */ }
			}

//			System.out.println("Creating '" + inCanonicalName + "' ...");

			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	// FIXME - pretty lame!
	public Collection<CanonicalItem> listAll() throws IOException {
		return esUtils.listAll( itemIndexUrl, CanonicalItem.class);
	}

    public long countAll() throws IOException {
        return esUtils.countAll(itemIndexUrl);
    }

    @Override
    public void delete( final ICanonicalItem inItem) throws IOException {
        eventService.deleteItem(inItem);
        throw new RuntimeException("unimpl");
    }

    @Override
    public void deleteNow( final ICanonicalItem inItem) throws IOException {
        eventService.deleteItem(inItem);
        throw new RuntimeException("unimpl");
    }

	public void deleteAll() throws IOException {
		try {
			esClient.admin().indices().prepareDeleteMapping().setIndices("recipe").setType("items").execute().actionGet();
		}
		catch (TypeMissingException e) {
			// Ignore
		}
		catch (IndexMissingException e) {
			// Ignore
		}
	}

	/**
	 * @param items
	 * @return
	 */
	public List<ICanonicalItem> getAll( final List<Long> inIds) throws IOException {
        final List<ICanonicalItem> results = Lists.newArrayList();

        for ( final Long eachId : inIds) {
            Optional<ICanonicalItem> oI = getById(eachId);

            if (oI.isPresent()) {  // Shouldn't happen in Production!!
                results.add( oI.get() );
            }
        }

        return results;
	}
}