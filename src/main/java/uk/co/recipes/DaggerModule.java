/**
 * 
 */
package uk.co.recipes;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import uk.co.recipes.corr.Correlations;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.JacksonFactory;
import uk.co.recipes.persistence.RecipeFactory;
import dagger.Module;
import dagger.Provides;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
@Module(injects={CanonicalItemFactory.class, RecipeFactory.class, ItemsLoader.class, IngredientParser.class, TestDataUtils.class, Correlations.class, ObjectMapper.class})
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
	@Named("elasticSearchItemsUrl")
	String provideEsItemsUrl() {
		return "http://localhost:9200/recipe/items";
	}

	@Provides
	@Named("elasticSearchRecipesUrl")
	String provideEsRecipesUrl() {
		return "http://localhost:9200/recipe/recipes";
	}
}

