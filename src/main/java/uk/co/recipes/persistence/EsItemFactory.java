/**
 * 
 */
package uk.co.recipes.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.service.api.IItemPersistence;

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
    IEventService eventService;


	public ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
		final HttpPost req = new HttpPost( itemIndexUrl + "/" + inId);

		try {
			inItem.setId( sequences.getSeqnoForType("items_seqno") );

			req.setEntity( new StringEntity( mapper.writeValueAsString(inItem) ) );

			final HttpResponse resp = httpClient.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );

			eventService.addItem(inItem);
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inItem;
	}

	public ICanonicalItem getById( String inId) throws IOException {
		return mapper.readValue( mapper.readTree( new URL( itemIndexUrl + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

    public Optional<ICanonicalItem> getById( long inId) throws IOException {
        if ( inId >= Recipe.BASE_ID) {  // Just in case...
            return Optional.absent();
        }

        return esUtils.findOneByIdAndType( itemIndexUrl, inId, ICanonicalItem.class, CanonicalItem.class);
    }

    public String toStringId( final ICanonicalItem inItem) throws IOException {
        return toId( inItem.getCanonicalName() );
    }

	public static String toId( final String inCanonicalName) throws IOException {
		checkArgument( !inCanonicalName.contains(","), "Name should not contain comma: '" + inCanonicalName + "'");
		return inCanonicalName.toLowerCase().replace( ' ', '_');
	}

	public Optional<ICanonicalItem> get( final String inCanonicalName) throws IOException {
		try {
			return Optional.fromNullable( getById( toId(inCanonicalName) ) );
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
	public Collection<CanonicalItem> listAll() throws JsonParseException, JsonMappingException, IOException {
		return esUtils.listAll( itemIndexUrl, CanonicalItem.class);
	}

    // FIXME - pretty lame!
    public int countAll() throws IOException {
        return esUtils.countAll( itemIndexUrl, CanonicalItem.class);
    }

	public void deleteAll() throws IOException {
		final HttpResponse resp = httpClient.execute( new HttpDelete(itemIndexUrl) );
		EntityUtils.consume( resp.getEntity() );
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