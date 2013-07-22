package uk.co.recipes.neo;

import java.io.IOException;
import java.util.Properties;

import org.reco4j.engine.RecommenderEngine;
import org.reco4j.graph.neo4j.Neo4JNode;
import org.reco4j.graph.neo4j.Neo4jGraph;
import org.reco4j.graph.neo4j.util.Neo4JPropertiesHandle;
import org.reco4j.recommender.IRecommender;
import org.testng.annotations.Test;

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
	public void recipesTest() throws IOException {

		Properties props = new Properties();
		props.setProperty("dbPath", "/private/tmp/neo4j");
//		props.setProperty("KValue", "2");
//		props.setProperty("DistanceAlgorithm", "3");
//		props.setProperty("recommenderType", "1");
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

		for (int i = 0; i < 300; i++) {
			try {
				Neo4JNode node = new Neo4JNode(i);
				System.out.println(i + ":> " + node.getRatingsFromUser(ph) + " / " + rec.recommend(node));
			} catch (Throwable t) {
				// ystem.err.println(t);
			}
		}
	}
}