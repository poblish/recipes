package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.service.taste.impl.MyrrixTasteRecommendationService;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private MyrrixTasteRecommendationService api = GRAPH.get( MyrrixTasteRecommendationService.class );

	@BeforeClass
	public void setUp() throws IOException, TasteException {
		final ClientRecommender recommender = GRAPH.get( ClientRecommender.class );
		recommender.ingest( new File("src/test/resources/taste/main.txt") );
		recommender.refresh();
	}

	@Test
	public void testMyrrixClient() throws IOException, TasteException {
		long userId = 1L;
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 7L, 6L, 8L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 2L, 8L, 7L, 6L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 8L, 4L, 3L, 1L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 6L, 1L, 3L, 4L, 8L) ));
		api.recommendIngredients( userId++, 10);  // No asserts: just too variable
	}
}
