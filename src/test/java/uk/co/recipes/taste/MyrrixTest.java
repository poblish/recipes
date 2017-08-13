package uk.co.recipes.taste;

import dagger.Component;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.ProductionMyrrixModule;
import uk.co.recipes.myrrix.MyrrixUpdater;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;
import uk.co.recipes.service.taste.impl.MyrrixTasteSimilarityService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

public class MyrrixTest {

    @Inject MyrrixTasteRecommendationService api;
    @Inject MyrrixRecommendationService fullApi;
    @Inject MyrrixTasteSimilarityService explorerApi;
    @Inject MyrrixUpdater updater;

    @BeforeClass
    public void setUp() throws IOException, TasteException {
        DaggerMyrrixTest_TestComponent.create().inject(this);

        updater.ingest(new File("src/test/resources/taste/main.txt"));
    }

    @Test(enabled = false)
    public void testMyrrixRecommendations() throws IOException, TasteException {
        long userId = 1000L;
        // FIXME - these are too unpredictable at the moment
//		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 7L, 6L, 8L) ));
//		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 2L, 8L, 7L, 6L) ));
//		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 8L, 4L, 3L, 1L) ));
//		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 6L, 1L, 3L, 4L, 8L) ));
        api.recommendIngredients(userId++, 10);  // No asserts: just too variable
    }

    @Test(enabled = false)
    public void testMyrrixSimilarity() throws IOException, TasteException {
        long userId = 1000L;
        // FIXME - these are too unpredictable at the moment
        explorerApi.similarIngredients(userId++, 10);
//		assertThat( explorerApi.similarIngredients( userId++, 10), is( Arrays.asList( 3L, 4L, 5L, 2L, 8L, 6L, 7L) ));
//		assertThat( explorerApi.similarIngredients( userId++, 10), is( Arrays.asList( 7L, 4L, 3L, 1L, 6L, 5L, 8L) ));
//		assertThat( explorerApi.similarIngredients( userId++, 10), is( Arrays.asList( 1L, 4L, 5L, 2L, 8L, 6L, 7L) ));
//		assertThat( explorerApi.similarIngredients( userId++, 10), is( Arrays.asList( 3L, 1L, 5L, 2L, 8L, 6L, 7L) ));
//		assertThat( explorerApi.similarIngredients( userId++, 10), is( Arrays.asList( 7L, 1L, 3L, 4L, 2L, 8L, 6L) ));
    }

    @Singleton
    @Component(modules = {DaggerModule.class, ProductionMyrrixModule.class})
    public interface TestComponent {
        void inject(final MyrrixTest runner);
    }
}