package uk.co.recipes.neo;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.neo4j.graphdb.NotFoundException;
import org.reco4j.engine.RecommenderEngine;
import org.reco4j.graph.neo4j.Neo4JNode;
import org.reco4j.graph.neo4j.Neo4jGraph;
import org.reco4j.graph.neo4j.util.Neo4JPropertiesHandle;
import org.reco4j.model.Rating;
import org.reco4j.recommender.IRecommender;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * 
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class Reco4JTest {

	// See: http://www.reco4j.org/get-started.jsp
	@Test
	public void testKnnWithCosineSimilarity() throws IOException {

		Properties props = new Properties();
		props.setProperty("dbPath", "/private/tmp/neo4j");
		props.setProperty("DistanceAlgorithm", "3");
		props.setProperty("recommenderType", "1");
		props.setProperty("recalculateSimilarity", "true");
		props.setProperty("userType", "USER");
		props.setProperty("userIdentifier", "name");
		props.setProperty("itemType", "INGREDIENT");
		props.setProperty("itemIdentifier", "name");
		props.setProperty("rankEdgeIdentifier", "RATING");
		props.setProperty("RankValueIdentifier", "score");

		Neo4JPropertiesHandle ph = Neo4JPropertiesHandle.getInstance();
		ph.setProperties(props);

		Neo4jGraph graphDB = new Neo4jGraph(ph);
		graphDB.initDatabase();

		IRecommender<?> rec = RecommenderEngine.buildRecommender(graphDB, props);

		List<String> gotNames = Lists.newArrayList();
		List<List<Rating>> gotActualRatings = Lists.newArrayList();
		List<List<Rating>> gotRecommendedRatings = Lists.newArrayList();

		for (int i = 50; i < 200 && gotNames.size() < 5; i++) {
			try {
				final Neo4JNode node = new Neo4JNode(i);
				if (!node.getProperty("type").equals("USER")) {
				    // No idea why: node.getRatingsFromUser(ph) works for these non-users too!!
				    continue;
				}

				gotNames.add( node.getProperty("name") );
				gotActualRatings.add( filterRatings( node.getRatingsFromUser(ph) ) );
				gotRecommendedRatings.add( filterRatings( rec.recommend(node, 20) ) );
			}
			catch (NotFoundException t) {
				// Ignore
			}
		}

		assertThat( gotNames, is( asList("User 1","User 2","User 3","User 4","User 5") ));
		assertThat( gotActualRatings.size(), is(5));
		assertThat( gotActualRatings, is( asList( asList( newRating( 11, 2.0), newRating( 36, 6.0), newRating( 43, 9.0), newRating( 22, 7.0)), asList( newRating( 48, 8.0), newRating( 11, 1.0), newRating( 36, 3.0), newRating( 22, 5.0)), asList( newRating( 40, 10.0), newRating( 44, 10.0), newRating( 43, 4.0)), asList( newRating( 40, 9.0), newRating( 48, 8.0), newRating( 43, 1.0)), asList( newRating( 18, 5.0) )) ));
		assertThat( gotRecommendedRatings, is( asList( asList( newRating( 44, 9.0), newRating( 40, 9.0), newRating( 48, 5.855755076535925)), asList( newRating( 40, 8.0), newRating( 43, 4.25)), asList( newRating( 48, 7.303061543300931), newRating( 11, 4.0), newRating( 36, 4.0), newRating( 22, 4.0)), asList( newRating( 44, 5.404082057734576), newRating( 11, 4.853571800517753), newRating( 36, 4.853571800517753), newRating( 22, 4.853571800517753)), new ArrayList<Rating>() ) ));

		graphDB.getGraphDB().shutdown();
	}

	// See: http://www.reco4j.org/get-started.jsp
	@Test
	public void testMahoutWithCosineSimilarity() throws IOException {

		Properties props = new Properties();
		props.setProperty("dbPath", "/private/tmp/neo4j");
		props.setProperty("DistanceAlgorithm", "3");
		props.setProperty("recommenderType", "4");
		props.setProperty("recalculateSimilarity", "true");
		props.setProperty("userType", "USER");
		props.setProperty("userIdentifier", "name");
		props.setProperty("itemType", "INGREDIENT");
		props.setProperty("itemIdentifier", "name");
		props.setProperty("rankEdgeIdentifier", "RATING");
		props.setProperty("RankValueIdentifier", "score");

		Neo4JPropertiesHandle ph = Neo4JPropertiesHandle.getInstance();
		ph.setProperties(props);

		Neo4jGraph graphDB = new Neo4jGraph(ph);
		graphDB.initDatabase();

		IRecommender<?> rec = RecommenderEngine.buildRecommender(graphDB, props);

		List<String> gotNames = Lists.newArrayList();
		List<List<Rating>> gotActualRatings = Lists.newArrayList();
		List<List<Rating>> gotRecommendedRatings = Lists.newArrayList();

		for (int i = 50; i < 200 && gotNames.size() < 5; i++) {
			try {
				final Neo4JNode node = new Neo4JNode(i);
				if (!node.getProperty("type").equals("USER")) {
				    // No idea why: node.getRatingsFromUser(ph) works for these non-users too!!
				    continue;
				}

				gotNames.add( node.getProperty("name") );
				gotActualRatings.add( filterRatings( node.getRatingsFromUser(ph) ) );
				gotRecommendedRatings.add( filterRatings( rec.recommend(node, 20) ) );
			}
			catch (NotFoundException t) {
				// Ignore
			}
		}

		assertThat( gotNames, is( asList("User 1","User 2","User 3","User 4","User 5") ));
		assertThat( gotActualRatings.size(), is(5));
		assertThat( gotActualRatings, is( asList( asList( newRating( 11, 2.0), newRating( 36, 6.0), newRating( 43, 9.0), newRating( 22, 7.0)), asList( newRating( 48, 8.0), newRating( 11, 1.0), newRating( 36, 3.0), newRating( 22, 5.0)), asList( newRating( 40, 10.0), newRating( 44, 10.0), newRating( 43, 4.0)), asList( newRating( 40, 9.0), newRating( 48, 8.0), newRating( 43, 1.0)), asList( newRating( 18, 5.0) )) ));
		assertThat( gotRecommendedRatings, is( asList( asList( newRating( 40, 9.0), newRating( 44, 9.0)), asList( newRating( 40, 8.0) ), asList( newRating( 48, 6.99491024017334), newRating( 22, 4.0), newRating( 11, 4.0), newRating( 36, 4.0)), asList( newRating( 44, 4.991103649139404) ), new ArrayList<Rating>() ) ));

		graphDB.getGraphDB().shutdown();
	}

	private Rating newRating( final long inNodeId, final double inScore) {
		return new Rating( new Neo4JNode(inNodeId), inScore);
	}

	private List<Rating> filterRatings( final List<Rating> inRatings) {
		return FluentIterable.from(inRatings).filter( new Predicate<Rating>() {
			public boolean apply( final Rating r) {
				return r.getRate() > 0.0;
			}
		} ).toList();
	}
}