package uk.co.recipes.myrrix;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.LoggerFactory;

import uk.co.recipes.persistence.EsUtils;
import uk.co.recipes.persistence.JacksonFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import dagger.Module;
import dagger.Provides;

@Module
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
    Client provideEsClient(final ConfigurationProvider config) {
        try {
            final Settings settings = Settings.builder()
                    .put("client.transport.ignore_cluster_name", true)
                    .build();

            final String esHost = config.getProperty("elasticsearch.host", String.class);
            final int esPort = config.getProperty("elasticsearch.port", Integer.class);

            final Client c = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));

            final String settingsStr = Files.toString( config.getProperty("elasticsearch.settingsPath", File.class), Charset.forName("utf-8"));

            if (!c.admin().indices().exists( new IndicesExistsRequest("recipe") ).actionGet().isExists()) {
                c.admin().indices().prepareCreate("recipe").setSettings(settingsStr).execute().actionGet();
            }

//            try {
//                c.admin().indices().prepareUpdateSettings("recipe").setSettings(settingsStr).execute().actionGet();
//            }
//            catch (RuntimeException /* Was ElasticSearchIllegalArgumentException */ e) {
//                c.admin().indices().prepareClose("recipe").execute().actionGet();
//                c.admin().indices().prepareUpdateSettings("recipe").setSettings(settingsStr).execute().actionGet();
//            }
//            finally {
            // try {
//                	c.admin().indices().prepareOpen("recipe").execute().actionGet();
//                }
//                catch (IndexNotFoundException e) {
//                	c.admin().indices().prepareCreate("recipe").setSettings(settingsStr).execute().actionGet();
//                }

            EsUtils.addPartialMatchMappings(c, config);
//            }

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