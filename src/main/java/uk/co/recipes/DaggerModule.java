package uk.co.recipes;

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
import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexMissingException;
import org.slf4j.LoggerFactory;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.DefaultEventService;
import uk.co.recipes.persistence.EsUtils;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.DefaultIngredientQuantityScoreBooster;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Module
public class DaggerModule {

//    @Inject
//    @Named("elasticSearchItemsUrl") String elasticSearchItemsUrl;

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
    public ConfigurationSource fileBasedConfig() {
        return new FilesConfigurationSource(() -> Paths.get("recipes_config.yml"));
    }

    @Provides
    @Singleton
    public ConfigurationProvider configurationProvider(final ConfigurationSource source) {
        return new ConfigurationProviderBuilder().withConfigurationSource(source).build();
    }

	@Provides
	@Singleton
	Client provideEsClient(final ConfigurationProvider config)  {
        try {
            final Client c = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

            final String settingsStr = Files.toString( config.getProperty("elasticsearch.settingsPath", File.class), Charset.forName("utf-8"));

//            if (!c.admin().indices().exists( new IndicesExistsRequest("recipe") ).actionGet().isExists()) {
//                c.admin().indices().prepareCreate("recipe").setSettings(settingsStr).execute().actionGet();
//            }

            try {
                c.admin().indices().prepareUpdateSettings("recipe").setSettings(settingsStr).execute().actionGet();
            }
            catch (ElasticSearchIllegalArgumentException /* Was ElasticSearchIllegalArgumentException */ e) {
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

                EsUtils.addPartialMatchMappings(c, config);
            }

            return c;
        }
        catch (IOException e) {
           throw new RuntimeException(e);
        }
	}

	@Provides
	@Named("elasticSearchItemsUrl")
	String provideEsItemsUrl(final ConfigurationProvider config) {
		return config.getProperty("elasticSearchItemsUrl", String.class);
	}

	@Provides
	@Named("elasticSearchUsersUrl")
	String provideEsUsersUrl(final ConfigurationProvider config) {
		return config.getProperty("elasticSearchUsersUrl", String.class);
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
    List<String> providePrefixAdjustments(final ConfigurationProvider config) {
        try {
            return commentedFileToStrings( config.getProperty("prefixAdjustmentsPath", File.class) );
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Provides
    @Singleton
    @Named("suffixAdjustments")
    List<String> provideSuffixAdjustments(final ConfigurationProvider config) {
        try {
            return commentedFileToStrings( config.getProperty("suffixAdjustmentsPath", File.class) );
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<String> commentedFileToStrings( final File inFile) throws IOException {
        return Files.readLines( inFile, Charset.forName("utf-8"), new LineProcessor<List<String>>() {
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
	Map<String,String> provideCuisineColours(final ObjectMapper mapper, final ConfigurationProvider config) {
		try {
			return mapper.readValue( config.getProperty("cuisineColoursPath", File.class), mapper.getTypeFactory().constructMapType( HashMap.class, String.class, String.class));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
