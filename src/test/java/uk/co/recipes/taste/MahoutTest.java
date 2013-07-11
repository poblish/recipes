package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.ItemUserAverageRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.testng.annotations.Test;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class MahoutTest {

	@Test
	public void testSlopeOneRecommender() throws IOException, TasteException {
		final FileDataModel model = new FileDataModel( new File("src/test/resources/taste/main.txt") );
		final Recommender recommender = new SlopeOneRecommender(model);
		assertThat( recommender.recommend(1L, 10).toString(), is("[RecommendedItem[item:7, value:16.0]]"));
		assertThat( recommender.recommend(2L, 10).toString(), is("[RecommendedItem[item:2, value:4.6666665]]"));
		assertThat( recommender.recommend(3L, 10).toString(), is("[RecommendedItem[item:1, value:6.0], RecommendedItem[item:3, value:4.5], RecommendedItem[item:4, value:1.0]]"));
		assertThat( recommender.recommend(4L, 10).toString(), is("[RecommendedItem[item:1, value:6.0], RecommendedItem[item:3, value:4.5], RecommendedItem[item:4, value:1.0]]"));
		assertThat( recommender.recommend(5L, 10).toString(), is("[RecommendedItem[item:1, value:6.0], RecommendedItem[item:2, value:4.6666665], RecommendedItem[item:3, value:4.5], RecommendedItem[item:4, value:1.0]]"));
	}

	@Test
	public void testItemUserAverageRecommender() throws IOException, TasteException {
		final FileDataModel model = new FileDataModel( new File("src/test/resources/taste/main.txt") );
		final Recommender recommender = new ItemUserAverageRecommender(model);
		assertThat( recommender.recommend(1L, 10).toString(), is("[RecommendedItem[item:6, value:10.133333], RecommendedItem[item:7, value:9.633333], RecommendedItem[item:5, value:8.133333]]"));
		assertThat( recommender.recommend(2L, 10).toString(), is("[RecommendedItem[item:7, value:7.883333], RecommendedItem[item:2, value:3.05]]"));
		assertThat( recommender.recommend(3L, 10).toString(), is("[RecommendedItem[item:5, value:10.133333], RecommendedItem[item:1, value:8.133333], RecommendedItem[item:3, value:6.633333], RecommendedItem[item:4, value:3.6333334]]"));
		assertThat( recommender.recommend(4L, 10).toString(), is("[RecommendedItem[item:6, value:10.133333], RecommendedItem[item:1, value:6.133333], RecommendedItem[item:3, value:4.633333], RecommendedItem[item:4, value:1.6333333]]"));
		assertThat( recommender.recommend(5L, 10).toString(), is("[]"));
	}
}
