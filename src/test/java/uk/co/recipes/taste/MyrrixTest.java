package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.elasticsearch.common.base.Throwables;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.service.taste.api.ITasteRecommendationsAPI;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixTest {

	private static ClientRecommender RECOMMENDER;

	@BeforeClass
	public void setUp() throws IOException, TasteException {
		final MyrrixClientConfiguration clientConfig = new MyrrixClientConfiguration();
		clientConfig.setHost("localhost");
		clientConfig.setPort(8080);

		// RECOMMENDER = new TranslatingClientRecommender( new ClientRecommender(clientConfig) );
		RECOMMENDER = new ClientRecommender(clientConfig);
		RECOMMENDER.ingest( new File("src/test/resources/taste/main.txt") );
		RECOMMENDER.refresh();
	}

	@Test
	public void testMyrrixClient() throws IOException, TasteException {
		final ITasteRecommendationsAPI api = new TestMyrrixRecsApi();

		long userId = 1L;
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 7L, 6L, 8L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 2L, 8L, 7L, 6L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 5L, 8L, 4L, 3L, 1L) ));
		assertThat( api.recommendIngredients( userId++, 10), is( Arrays.asList( 6L, 1L, 3L, 4L, 8L) ));
		api.recommendIngredients( userId++, 10);  // No asserts: just too variable
	}

	private static class TestMyrrixRecsApi implements ITasteRecommendationsAPI {

		@Override
		public List<Long> recommendIngredients( long inUser, int inNumRecs) {
			try {
				return getRecommendationUsers( RECOMMENDER.recommend( inUser, inNumRecs) );
			}
			catch (TasteException e) {
				throw Throwables.propagate(e);  // Yuk, FIXME, let's get the API right
			}
		}

		private static List<Long> getRecommendationUsers( final List<RecommendedItem> inItems) {
			System.out.println(inItems);
			return FluentIterable.from(inItems).transform( new Function<RecommendedItem,Long>() {

				@Override
				public Long apply( RecommendedItem input) {
					return input.getItemID();
				}
			} ).toList();
		}

		@Override
		public List<Long> recommendRecipes( long inUser, int inNumRecs) {
			throw new RuntimeException("unimpl");
		}		
	}
}
