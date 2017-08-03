package uk.co.recipes.persistence;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.cfg4j.provider.ConfigurationProvider;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequestBuilder;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 *
 * @author andrewregan
 */
public class EsUtils {

    @Inject
    Client esClient;
    @Inject
    ObjectMapper mapper;

    private final static Logger LOG = LoggerFactory.getLogger(EsUtils.class);

    @Inject
    public EsUtils() {
        // For Dagger
    }

    public JsonParser parseSource(final String inUrlString) throws IOException {
        return mapper.readTree(new URL(inUrlString)).path("_source").traverse();
    }

    public JsonParser parseSource(final JsonNode inJacksonNode) {
        return inJacksonNode.path("_source").traverse();
    }

    public <T> Optional<T> findOneByIdAndType(final String inBaseUrl, final long inId, final Class<T> inIfClazz, final Class<? extends T> inImplClazz) throws IOException {
        final Iterator<JsonNode> nodeItr = mapper.readTree(new URL(inBaseUrl + "/_search?q=id:" + inId + "&size=1")).path("hits").path("hits").iterator();
        if (nodeItr.hasNext()) {
            return Optional.fromNullable(mapper.readValue(parseSource(nodeItr.next()), inImplClazz));
        }

        return Optional.absent();
    }

    public <T> long countAll(final String inType) {
        return esClient.prepareSearch("recipe").setTypes(inType).setSize(0).execute().actionGet().getHits().totalHits();
    }

    public void waitUntilTypesRefreshed(final String... inTypes) {
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
            while (waitsToGo > 0 && reqBuilder.execute().actionGet().getTotal().getRefresh().getTotal() == currCount);
        } catch (InterruptedException e) {
            Throwables.propagate(e)
            ;
        }

        if (waitsToGo <= 0) {
            throw new RuntimeException("Timeout exceeded!");
        }
    }

    public List<IRecipe> deserializeRecipeHits(final SearchHit[] inHits) throws IOException {
        if (inHits.length == 0) {
            return Collections.emptyList();
        }

        final List<IRecipe> results = Lists.newArrayList();

        for (final SearchHit eachHit : inHits) {
            results.add(mapper.readValue(toBytes(eachHit.getSourceRef()), Recipe.class));
        }

        return results;
    }

    public List<IRecipe> deserializeRecipeHits(final MultiGetResponse inResponses) throws IOException {
        if (inResponses.getResponses().length == 0) {
            return Collections.emptyList();
        }

        final List<IRecipe> results = Lists.newArrayList();

        for (final MultiGetItemResponse eachHit : inResponses.getResponses()) {
            if (!eachHit.getResponse().isExists()) {
                LOG.warn("deserializeRecipeHits() Could not load Recipe " + eachHit.getId() + ", skipping.");
                continue;
            }
            results.add(mapper.readValue(eachHit.getResponse().getSourceAsBytes(), Recipe.class));
        }

        return results;
    }

    public List<ICanonicalItem> deserializeItemHits(final MultiGetResponse inResponses) throws IOException {
        if (inResponses.getResponses().length == 0) {
            return Collections.emptyList();
        }

        final List<ICanonicalItem> results = Lists.newArrayList();

        for (final MultiGetItemResponse eachHit : inResponses.getResponses()) {
            if (!eachHit.getResponse().isExists()) {
                LOG.warn("deserializeItemHits() Could not load Item " + eachHit.getId() + ", skipping.");
                continue;
            }
            results.add(mapper.readValue(eachHit.getResponse().getSourceAsBytes(), CanonicalItem.class));
        }

        return results;
    }

    public static void addPartialMatchMappings(final Client inClient, final ConfigurationProvider config) throws IOException {
        inClient.admin().indices().preparePutMapping("recipe").setType("items").setSource(Files.toString(config.getProperty("elasticsearch.mappings.items.path", File.class), Charset.forName("utf-8"))).execute().actionGet();
        inClient.admin().indices().preparePutMapping("recipe").setType("recipes").setSource(Files.toString(config.getProperty("elasticsearch.mappings.recipes.path", File.class), Charset.forName("utf-8"))).execute().actionGet();
    }

    public static Function<AnalyzeResponse.AnalyzeToken,String> getAnalyzeTokenToStringFunc() {
        return AnalyzeResponse.AnalyzeToken::getTerm;
    }

    public static byte[] toBytes(final BytesReference ref) {
        return ref.toBytes();
        // return BytesReference.toBytes(ref);
    }

    public QueryStringQueryBuilder queryString(final String q) {
        return org.elasticsearch.index.query.QueryBuilders.queryString(q);
        // return org.elasticsearch.index.query.QueryBuilders.queryStringQuery(q);
    }

    public ActionResponse deleteAllByType(final String index, final String type) {
        return esClient.admin().indices().prepareDeleteMapping().setIndices(index).setType(type).execute().actionGet();

        // https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-delete-by-query.html
//		return DeleteByQueryAction.INSTANCE.newRequestBuilder(esClient)
//				.filter(typeQuery(type))
//				.source(index)
//				.get();
    }
}