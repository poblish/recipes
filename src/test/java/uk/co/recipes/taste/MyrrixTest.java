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
import org.testng.annotations.Test;

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

	@Test
	public void testMyrrixClient() throws IOException, TasteException {
		final MyrrixClientConfiguration clientConfig = new MyrrixClientConfiguration();
		clientConfig.setHost("localhost");
		clientConfig.setPort(8080);

		// TranslatingRecommender recommender = new TranslatingClientRecommender( new ClientRecommender(clientConfig) );
		final ClientRecommender recommender = new ClientRecommender(clientConfig);
		recommender.ingest( new File("src/test/resources/taste/main.txt") );
		recommender.refresh();

		long userId = 1L;
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 5, 7, 6, 8) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 2, 8, 7, 6) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 5, 8, 4, 3, 1) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 6, 1, 3, 4, 8) ));
		getRecommendationUsers( recommender.recommend( userId++, 10) );  // No asserts: just too variable
	}

	final List<Integer> getRecommendationUsers( final List<RecommendedItem> inItems) {
		System.out.println(inItems);
		return FluentIterable.from(inItems).transform( new Function<RecommendedItem,Integer>() {

			@Override
			public Integer apply( RecommendedItem input) {
				return /* FIXME */ (int) input.getItemID();
			}
		} ).toList();
	}
}
