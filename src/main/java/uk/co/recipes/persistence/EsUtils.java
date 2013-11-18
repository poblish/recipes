/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.recipes.Recipe;
import uk.co.recipes.api.IRecipe;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class EsUtils {

	@Inject Client esClient;
	@Inject ObjectMapper mapper;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger( EsUtils.class );


	public JsonParser parseSource( final String inUrlString) throws IOException {
		return mapper.readTree( new URL(inUrlString) ).path("_source").traverse();
	}

	public JsonParser parseSource( final JsonNode inJacksonNode) {
		return inJacksonNode.path("_source").traverse();
	}

	public <T> Optional<T> findOneByIdAndType( final String inBaseUrl, final long inId, final Class<T> inIfClazz, final Class<? extends T> inImplClazz) throws IOException {
		final Iterator<JsonNode> nodeItr = mapper.readTree( new URL( inBaseUrl + "/_search?q=id:" + inId + "&size=1") ).path("hits").path("hits").iterator();
		if (nodeItr.hasNext()) {
			return Optional.fromNullable((T) mapper.readValue( parseSource( nodeItr.next() ), inImplClazz) );
		}

		return Optional.absent();
	}

	// FIXME - pretty lame!
	public <T> Collection<T> listAll( final String inBaseUrl, Class<T> inClass) throws IOException {
		final JsonNode allNodes = mapper.readTree( new URL( inBaseUrl + "/_search?q=*&size=9999") ).path("hits").path("hits");

		final Collection<T> allItems = Lists.newArrayList();

		for ( JsonNode each : allNodes) {
			allItems.add( mapper.readValue( parseSource(each), inClass));
		}

		return allItems;
	}

    public <T> long countAll( final String inBaseUrl) throws IOException {
        return mapper.readTree( new URL( inBaseUrl + "/_count") ).get("count").asLong();
    	// FIXME Throws weird errors: return esClient.prepareCount(inIndex).execute().actionGet().count();
    }

	public void waitUntilTypesRefreshed( final String... inTypes) {
		final IndicesStatsRequestBuilder reqBuilder = esClient.admin().indices().prepareStats("recipe").setRefresh(true).setTypes(inTypes);
		final long currCount = reqBuilder.execute().actionGet().getTotal().getRefresh().getTotal();
		int waitsToGo = 10;

//		LOG.info("Is... "  + currCount);

		try {
			do {
				Thread.sleep(250);
	//			LOG.info("Now... "  + reqBuilder.execute().actionGet().getTotal().getRefresh().getTotal() + ", waitsToGo = " + waitsToGo);
				waitsToGo--;
			}
			while ( waitsToGo > 0 && reqBuilder.execute().actionGet().getTotal().getRefresh().getTotal() == currCount);
		}
		catch (InterruptedException e) {
			Throwables.propagate(e)
;		}

		if ( waitsToGo <= 0) {
			throw new RuntimeException("Timeout exceeded!");
		}
	}

	public List<IRecipe> deserializeRecipeHits( final SearchHit[] inHits) throws IOException {
		if ( inHits.length == 0) {
			return Collections.emptyList();
		}

		final List<IRecipe> results = Lists.newArrayList();

		for ( final SearchHit eachHit : inHits) {
			results.add( mapper.readValue( eachHit.getSourceAsString(), Recipe.class) );
		}

		return results;
	}

	public static void addPartialMatchMappings( final Client inClient) throws ElasticSearchException, IOException {
		// FIXME - absolute paths!
	    final String homeDir = System.getProperty("user.home");
		inClient.admin().indices().preparePutMapping("recipe").setType("items").setSource( Files.toString( new File( homeDir + "/Development/java/recipe_explorer/src/main/resources/esItemsMappingsAutocomplete.json"), Charset.forName("utf-8")) ).execute().actionGet();
		inClient.admin().indices().preparePutMapping("recipe").setType("recipes").setSource( Files.toString( new File( homeDir + "/Development/java/recipe_explorer/src/main/resources/esRecipesMappingsAutocomplete.json"), Charset.forName("utf-8")) ).execute().actionGet();
	}

	public static Function<AnalyzeResponse.AnalyzeToken,String> getAnalyzeTokenToStringFunc() {
		return new Function<AnalyzeResponse.AnalyzeToken,String>() {

            @Override
            public String apply( final AnalyzeToken input) {
                return input.getTerm();
            }
        };
	}
}