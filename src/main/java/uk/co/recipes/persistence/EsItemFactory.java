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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.HttpClient;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.TypeMissingException;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IItemPersistence;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsItemFactory implements IItemPersistence {

	private final static Logger LOG = LoggerFactory.getLogger( EsItemFactory.class );

	@Inject Client esClient;
	@Inject HttpClient httpClient;

	@Inject @Named("elasticSearchItemsUrl")
	String itemIndexUrl;

	@Inject ObjectMapper mapper;
	@Inject EsUtils esUtils;
	@Inject EsSequenceFactory sequences;
	@Inject MetricRegistry metrics;
	@Inject IEventService eventService;

	@Inject Cache<String,ICanonicalItem> itemsCache;


	public ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_PUTS).time();

		try {
			inItem.setId( sequences.getSeqnoForType("items_seqno") );

			/* IndexResponse esResp = */ esClient.prepareIndex( "recipe", "items", inId)/*.setCreate(true) */.setSource( mapper.writeValueAsString(inItem) ).execute().actionGet();

			itemsCache.put( inId, inItem);

			eventService.addItem(inItem);
		}
		finally {
			timerCtxt.stop();
		}

		return inItem;
	}

	public ICanonicalItem getByName( String inId) throws IOException {
        final Timer.Context timerCtxt = metrics.timer(TIMER_ITEMS_NAME_GETS).time();

        final ICanonicalItem cachedItem = itemsCache.getIfPresent(inId);
        if ( cachedItem != null) {
        	return cachedItem;  // Cache HIT
        }

		try {
			final GetResponse resp = esClient.prepareGet( "recipe", "items", inId).get();
			if (!resp.isExists()) {
				return null;  // Nothing to do - cannot cache value of null anyway. Can this actually happen?!?
			}

			final ICanonicalItem item = mapper.readValue( resp.getSourceAsString(), CanonicalItem.class);
			itemsCache.put( inId, item);  // Cache MISS
			return item;
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
			final ICanonicalItem itemOrNull = getOrCreateImpl( inCanonicalName, inMatchAliases, /* Deliberate: */ inMatchAliases);
			if ( itemOrNull != null) {
				return itemOrNull;
			}

//			System.out.println("Creating '" + inCanonicalName + "' ...");

			return put( inCreator.get(), toId(inCanonicalName));
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	public ICanonicalItem getOrCreateImpl( final String inCanonicalName, final boolean inMatchAliases, final boolean inFindSubItems) {
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
//							LOG.info("Successfully mapped Alias '" + inCanonicalName + "' => " + mappedAlias);
							return mappedAlias;
						}
					}
					else if ( hits.length > 1) {
                        final ICanonicalItem firstMatch = mapper.readValue( hits[0].getSourceAsString(), CanonicalItem.class);

                        // Yuk - check for *exact* alias match
                        for ( String eachAlias : ((CanonicalItem) firstMatch).getAliases()) {
                            if (eachAlias.equalsIgnoreCase(inCanonicalName)) {
                                return firstMatch;
                            }
                        }

						LOG.warn("Too many matches for Alias '" + inCanonicalName + "' - ignoring '" + firstMatch + "' and '" + mapper.readValue( hits[1].getSourceAsString(), CanonicalItem.class) + "'");
					}
				}
				catch (IOException e) { /* e.printStackTrace(); */ }
			}

			// A bit of a hack to deal with things like "British red-skinned eating apples". Use 'inMatchAliases' as proxy for 'discovered' item.
			if (inFindSubItems) {
				if (inCanonicalName.contains(" ")) {
					final String[] words = inCanonicalName.split(" ");
					for ( int numWordsToRemove = 1; numWordsToRemove < words.length; numWordsToRemove++) {

						final String candidate1 = Joiner.on(' ').join( Arrays.copyOfRange( words, numWordsToRemove, words.length) );
						final ICanonicalItem item1 = getOrCreateImpl(candidate1, /* Want aliases: */ true, false);
						if (item1 != null) {
							LOG.trace("Convert '" + inCanonicalName + "' => " + item1);
							return item1;
						}

						final String candidate2 = Joiner.on(' ').join( Arrays.copyOfRange( words, 0, words.length - numWordsToRemove) );
						final ICanonicalItem item2 = getOrCreateImpl(candidate2, /* Want aliases: */ true, false);
						if (item2 != null) {
							LOG.trace("Convert '" + inCanonicalName + "' => " + item2);
							return item2;
						}
					}
				}
			}

			return null;
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

    public long countAll() throws IOException {
        return esUtils.countAll("items");
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

            EsUtils.addPartialMatchMappings(esClient);
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
	    final Timer.Context timerCtxt = metrics.timer("items.getAll").time();

	    // FIXME OK, we can't use ES GET methods, because 'id' is the name, not this numeric Id
	    // But the least we could do is use the ES MultiSearch API, not n individual searches!

		try {
	        final List<ICanonicalItem> results = Lists.newArrayList();
	
	        for ( final Long eachId : inIds) {
	            Optional<ICanonicalItem> oI = getById(eachId);
	
	            if (oI.isPresent()) {  // Shouldn't happen in Production!!
	                results.add( oI.get() );
	            }
	        }
	
	        return results;
		}
		finally {
			timerCtxt.close();
		}
	}

	public Optional<ICanonicalItem> findBestMatchByName( final String[] inNames) throws IOException {
		for ( String eachName : inNames) {
			Optional<ICanonicalItem> optItem = get(eachName);
			if (optItem.isPresent()) {
				return optItem;
			}
		}

		return Optional.absent();
	}

	public void waitUntilRefreshed() {
		esUtils.waitUntilTypesRefreshed("items");
	}
}