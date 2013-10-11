/**
 * 
 */
package uk.co.recipes.myrrix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexMissingException;
import org.slf4j.LoggerFactory;

import uk.co.recipes.persistence.EsUtils;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.service.impl.EsSearchService;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import dagger.Module;
import dagger.Provides;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
@Module(injects={ObjectMapper.class, ClientRecommender.class, EsSearchService.class, MetricRegistry.class, RecipesRescorer.class})
public class RescorerModule {

	@Provides
	@Singleton
	ObjectMapper provideObjectMapper() {
		final ObjectMapper inst = new ObjectMapper();
		JacksonFactory.initialiseMapper(inst);
		return inst;
	}

	@Provides
	@Singleton
	Client provideEsClient() {
		final Client c = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        try {
            final String homeDir = System.getProperty("user.home");
            final String settingsStr = Files.toString( new File( homeDir + "/Development/java/recipe_explorer/src/main/resources/index.yaml"), Charset.forName("utf-8"));

            try {
                c.admin().indices().prepareUpdateSettings("recipe").setSettings(settingsStr).execute().actionGet();
            }
            catch (ElasticSearchIllegalArgumentException e) {
                c.admin().indices().prepareClose("recipe").execute().actionGet();
                c.admin().indices().prepareUpdateSettings("recipe").setSettings(settingsStr).execute().actionGet();
            }
            finally {
                try {
                	c.admin().indices().prepareOpen("recipe").execute().actionGet();
                }
                catch (IndexMissingException e) {
                	c.admin().indices().prepareCreate("recipe").setSettings(settingsStr).execute().actionGet();
                }

                EsUtils.addPartialMatchMappings(c);
            }
        }
        catch (IOException e) {
           Throwables.propagate(e);
        }

		return c;
	}

	@Provides
	@Named("elasticSearchItemsUrl")
	String provideEsItemsUrl() {
		return "http://localhost:9200/recipe/items";
	}

	@Provides
	@Named("elasticSearchRecipesUrl")
	String provideEsRecipesUrl() {
		return "http://localhost:9200/recipe/recipes";
	}

    @Provides
    @Singleton
    MetricRegistry provideMetricRegistry() {
        final MetricRegistry registry = new MetricRegistry();
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start( 1, TimeUnit.MINUTES);
        return registry;
    }
}