package uk.co.recipes;

import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;

import javax.inject.Inject;
import java.io.IOException;

/**
 * TODO
 *
 * @author andrewregan
 */
public abstract class AbstractEsDataTest {

    @Inject
    protected Client esClient;

    @Inject
    protected IItemPersistence itemFactory;

    @Inject
    protected IRecipePersistence recipeFactory;

    @BeforeClass
    public void cleanIndices() throws IOException {
        itemFactory.deleteAll();
        recipeFactory.deleteAll();
    }

    @AfterClass(alwaysRun = true)
    public void closeElasticsearch() {
        esClient.close();
    }
}