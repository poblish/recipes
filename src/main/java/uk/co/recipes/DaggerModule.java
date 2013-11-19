/**
 * 
 */
package uk.co.recipes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexMissingException;
import org.slf4j.LoggerFactory;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.DefaultEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.parse.NameAdjuster;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.EsUtils;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.DefaultIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;
import uk.co.recipes.test.TestDataUtils;
import uk.co.recipes.ui.CuisineColours;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import dagger.Module;
import dagger.Provides;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
@Module(injects={EsItemFactory.class, EsRecipeFactory.class, ItemsLoader.class, IngredientParser.class, TestDataUtils.class, ObjectMapper.class, ClientRecommender.class,
				 MyrrixTasteRecommendationService.class, MyrrixRecommendationService.class, MyrrixTasteSimilarityService.class, MyrrixExplorerService.class,
				 Client.class, EsSearchService.class, EsUserFactory.class, EsSequenceFactory.class, MyrrixUpdater.class, IEventService.class, UserRatings.class,
				 MetricRegistry.class, EsExplorerFilters.class, NameAdjuster.class, CuisineColours.class})
public class DaggerModule {

	@Provides
	@Singleton
	HttpClient provideHttpClient() {
		return HttpClientBuilder.create().build();
	}

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
	@Named("elasticSearchUsersUrl")
	String provideEsUsersUrl() {
		return "http://localhost:9200/recipe/users";
	}

    @Provides
    @Singleton
    IEventService provideEventService() {
        return new DefaultEventService();
    }

    @Provides
    @Singleton
    IIngredientQuantityScoreBooster provideIngredientQuantityScoreBooster() {
        return new DefaultIngredientQuantityScoreBooster();
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

	@Provides
	@Singleton
	ClientRecommender provideClientRecommender() {
		final MyrrixClientConfiguration clientConfig = new MyrrixClientConfiguration();
		clientConfig.setHost("localhost");
		clientConfig.setPort(8080);

		// TranslatingClientRecommender recommender = new TranslatingClientRecommender( new ClientRecommender(clientConfig) );
		try {
			return new ClientRecommender(clientConfig);
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

    @Provides
    @Singleton
    Cache<String,ICanonicalItem> provideItemCache() {
    	// Can't actually think of anything else useful...
    	return CacheBuilder.newBuilder().recordStats().maximumSize(1300).build();
    }

    @Provides
    @Singleton
    @Named("prefixAdjustments")
    List<String> providePrefixAdjustments() {
        try {
            return commentedFileToStrings( System.getProperty("user.home") + "/Development/java/recipe_explorer/src/main/resources/prefixAdjustments.txt");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Provides
    @Singleton
    @Named("suffixAdjustments")
    List<String> provideSuffixAdjustments() {
        try {
            return commentedFileToStrings( System.getProperty("user.home") + "/Development/java/recipe_explorer/src/main/resources/suffixAdjustments.txt");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<String> commentedFileToStrings( final String inPath) throws IOException {
        return Files.readLines( new File(inPath), Charset.forName("utf-8"), new LineProcessor<List<String>>() {
        	final List<String> result = Lists.newArrayList();

        	@Override
        	public boolean processLine(String line) {
        		if (!line.startsWith("// ")) {  // Ignore comments
        			result.add(line);
        		}
        		return true;
        	}

        	@Override
        	public List<String> getResult() {
        		return result;
        	}
        });
    }

	@Provides
	@Singleton
    @Named("cuisineColours")
	Map<String,String> provideCuisineColours( ObjectMapper mapper) {
		try {
			return mapper.readValue( new File( System.getProperty("user.home") + "/Development/java/recipe_explorer/src/test/resources/cuisineColours.json"), mapper.getTypeFactory().constructMapType( HashMap.class, String.class, String.class));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}