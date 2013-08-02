/**
 * 
 */
package uk.co.recipes;

import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IItemPersistence;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import dagger.ObjectGraph;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public abstract class AbstractEsDataTest {

	protected final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	protected Client esClient = GRAPH.get( Client.class );
	protected IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	protected IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@AfterClass(alwaysRun=true)
	public void closeElasticsearch() {
		esClient.close();
	}
}