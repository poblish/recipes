/**
 * 
 */
package uk.co.recipes;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import uk.co.recipes.corr.Correlations;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.events.impl.DefaultEventService;
import uk.co.recipes.events.impl.MyrrixUpdater;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.DefaultIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.EsSearchService;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;
import uk.co.recipes.test.TestDataUtils;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import dagger.Module;
import dagger.Provides;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
@Module(injects={EsItemFactory.class, EsRecipeFactory.class, ItemsLoader.class, IngredientParser.class, TestDataUtils.class, Correlations.class, ObjectMapper.class, ClientRecommender.class,
				 MyrrixTasteRecommendationService.class, MyrrixRecommendationService.class, MyrrixTasteSimilarityService.class, MyrrixExplorerService.class,
				 Client.class, EsSearchService.class, EsUserFactory.class, EsSequenceFactory.class, MyrrixUpdater.class, IEventService.class, UserRatings.class,
				 MetricRegistry.class})
public class DaggerModule {

	@Provides
	@Singleton
	HttpClient provideHttpClient() {
		return new DefaultHttpClient();
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
		return new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
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
        return new MetricRegistry();
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
}