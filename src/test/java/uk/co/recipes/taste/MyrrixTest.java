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

//		HttpClient hc = new DefaultHttpClient();
//		hc.execute( new HttpDelete("http://localhost:8080/pref/1/8") );
//		hc.execute( new HttpDelete("http://localhost:8080/pref/1/2") );
//		hc.execute( new HttpDelete("http://localhost:8080/pref/1/3") );
//		hc.execute( new HttpDelete("http://localhost:8080/pref/1/4") );

		// TranslatingRecommender recommender = new TranslatingClientRecommender( new ClientRecommender(clientConfig) );
		final ClientRecommender recommender = new ClientRecommender(clientConfig);
//		recommender.removePreference( 1L, 1L);
//		recommender.removePreference( 1L, 2L);
//		recommender.removePreference( 1L, 3L);
//		recommender.removePreference( 1L, 4L);
//		recommender.removePreference( 2L, 1L);
//		recommender.removePreference( 2L, 3L);
//		recommender.removePreference( 2L, 4L);
//		recommender.removePreference( 2L, 5L);
//		recommender.removePreference( 3L, 6L);
//		recommender.removePreference( 3L, 7L);
//		recommender.removePreference( 4L, 2L);
//		recommender.removePreference( 4L, 5L);
//		recommender.removePreference( 4L, 7L);
//		recommender.removePreference( 5L, 8L);
		recommender.ingest( new File("src/test/resources/taste/main.txt") );
		recommender.refresh();

		long userId = 1L;
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 5, 7, 6, 8) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 2, 8, 7, 6) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 5, 8, 4, 3, 1) ));
		assertThat( getRecommendationUsers( recommender.recommend( userId++, 10) ), is( Arrays.asList( 6, 1, 3, 4, 8) ));

		List<Integer> user5_Scores = getRecommendationUsers( recommender.recommend( userId++, 10) );  // Just too variable
//		assertThat( user5_Scores.indexOf(3), lessThan( user5_Scores.indexOf(7) ));  // 6 better than 3
//		assertThat( user5_Scores.indexOf(1), lessThan( user5_Scores.indexOf(8) ));  // 3 better than 8
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
