package uk.co.recipes.neo;

import java.io.IOException;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.server.Bootstrapper;
import org.reco4j.engine.RecommenderEngine;
import org.reco4j.graph.neo4j.Neo4JNode;
import org.reco4j.graph.neo4j.Neo4jGraph;
import org.reco4j.graph.neo4j.util.Neo4JPropertiesHandle;
import org.reco4j.recommender.IRecommender;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.persistence.CanonicalItemFactory;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Reco4JTest {

//	private GraphDatabaseService graphDb;
	private Bootstrapper bootstrapper = null;

	@BeforeClass
	public void prepareTestDatabase() {
//		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder("/private/tmp/neo4j")
//	    										.setConfig( GraphDatabaseSettings.node_keys_indexable, "name")
//	    										.setConfig( GraphDatabaseSettings.node_auto_indexing, "true")
//	    										.newGraphDatabase();

//		bootstrapper = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) graphDb);
//		bootstrapper.start();
//
//		final ExecutionEngine engine = new ExecutionEngine(graphDb);
//		engine.execute("start r=relationship(*) delete r");
//		engine.execute("start r=node(*) delete r");
	}

//	@BeforeClass
//	public void cleanIndices() throws ClientProtocolException, IOException {
//		CanonicalItemFactory.startES();
//		CanonicalItemFactory.deleteAll();
//		RecipeFactory.deleteAll();
//	}
//
//	@BeforeClass
//	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
//		ItemsLoader.load();
//		Thread.sleep(1000);
//	}

	@Test
	public void recipesTest() throws IOException {
//		Transaction tx = graphDb.beginTx();
//
//		try {
////			Recipe r = new Recipe("Lamb Cobbler");
//			Node recipeNode = graphDb.createNode( MyLabels.RECIPE );
//			recipeNode.setProperty( "name", "Cashew Curry");
//
//			final List<IIngredient> ings = parseIngredientsFrom("chCashBlackSpiceCurry.txt");
//
//			for ( IIngredient each : ings) {
//				Optional<Node> on = findItem( each.getItem() );
//				Node n = on.isPresent() ? on.get() : createItemHierarchy( each.getItem() );
//
//				n.createRelationshipTo( recipeNode, MyRelationshipTypes.CONTAINED_IN);
//
//				handleTagsForIIngredient( each, n);
//			}
////
////			RecipeStage rs = new RecipeStage();
////			rs.addIngredients(ings);
////			r.addStage(rs);
//
//			final Node foundNode = findItem("Cumin Seeds").get();
//			System.out.println("Props " + Lists.newArrayList( foundNode.getPropertyKeys() ));
//			System.out.println("Relns " + Lists.newArrayList( foundNode.getRelationships() ));
//			System.out.println(recipeNode);
//
//			final Node foundRecipe = findRecipe("Cashew Curry").get();
//			System.out.println("Props " + Lists.newArrayList( foundRecipe.getPropertyKeys() ));
//			System.out.println("Relns " + Lists.newArrayList( foundRecipe.getRelationships() ));
//
//			///////////////////////////////////////////////////////////////////////////////////////////
//
//			Node recipeNode2 = graphDb.createNode( MyLabels.RECIPE );
//			recipeNode2.setProperty( "name", "Thai Fish Curry");
//
//			final List<IIngredient> ings2 = parseIngredientsFrom("ttFishCurry.txt");
//
//			for ( IIngredient each : ings2) {
//				Optional<Node> on = findItem( each.getItem() );
//				Node n = on.isPresent() ? on.get() : createItemHierarchy( each.getItem() );
//
//				n.createRelationshipTo( recipeNode2, MyRelationshipTypes.CONTAINED_IN);
//
//                handleTagsForIIngredient( each, n);
//			}
//
//			///////////////////////////////////////////////////////////////////////////////////////////  Collaborative Filtering. Follow our existing Taste example: 1,1,7; 1,2,9; 1,3,6; 1,4,2; 2,1,5; 2,3,3; 2,4,1; 2,5,8; 3,2,4; 3,6,10; 3,7,10; 4,2,1; 4,5,8; 4,7,9; 5,8,5
//			///////////////////////////////////////////////////////////////////////////////////////////  Where Ingredient indexes are for: Cumin Seeds; Green Beans; Turmeric; Garlic Cloves; Basmati Rice; Tamarind Paste; Fennel Seed; Coriander
//
//			final Node user_1 = graphDb.createNode( MyLabels.USER );
//			user_1.setProperty( "name", "User 1");
//			rateItem( user_1, "Cumin Seeds", 7);
//			rateItem( user_1, "Green Beans", 9);
//			rateItem( user_1, "Turmeric", 6);
//			rateItem( user_1, "Garlic Cloves", 2);
//
//			final Node user_2 = graphDb.createNode( MyLabels.USER );
//			user_2.setProperty( "name", "User 2");
//			rateItem( user_2, "Cumin Seeds", 5);
//			rateItem( user_2, "Turmeric", 3);
//			rateItem( user_2, "Garlic Cloves", 1);
//			rateItem( user_2, "Basmati Rice", 8);
//
//			final Node user_3 = graphDb.createNode( MyLabels.USER );
//			user_3.setProperty( "name", "User 3");
//			rateItem( user_3, "Green Beans", 4);
//			rateItem( user_3, "Tamarind Paste", 10);
//			rateItem( user_3, "Fennel Seed", 10);
//
//			final Node user_4 = graphDb.createNode( MyLabels.USER );
//			user_4.setProperty( "name", "User 4");
//			rateItem( user_4, "Green Beans", 1);
//			rateItem( user_4, "Basmati Rice", 8);
//			rateItem( user_4, "Fennel Seed", 9);
//
//			final Node user_5 = graphDb.createNode( MyLabels.USER );
//			user_5.setProperty( "name", "User 5");
//			rateItem( user_5, "Coriander", 5);


		    Properties props = new Properties();
		    props.setProperty( "dbPath", "/private/tmp/neo4j");
		    props.setProperty( "userType", "USER");
		    props.setProperty( "userIdentifier", "name");
		    props.setProperty( "itemType", "INGREDIENT");
		    props.setProperty( "itemIdentifier", "name");
		    props.setProperty( "rated", "RATING");

		    Neo4JPropertiesHandle ph = Neo4JPropertiesHandle.getInstance();
		    ph.setProperties(props);

		    Neo4jGraph graphDB = new Neo4jGraph(ph);
////		    graphDB.setProperties(properties);
		    graphDB.initDatabase();

//		    org.reco4j.session.RecommenderSessionManager.getInstance().setLearningDataSet(graphDB);


		    IRecommender<?> rec = RecommenderEngine.buildRecommender(graphDB, props);

		for ( int i = 0; i < 300; i++) {
			try {
				Neo4JNode node = new Neo4JNode(i);
				System.out.println( i+ ":> " + node.getRatingsFromUser(ph) + " / " + rec.recommend(node));
			}
			catch (Throwable t) {
				// ystem.err.println(t);
			}
		}
	}

	enum MyRelationshipTypes implements RelationshipType {
		CONTAINED_IN, TAGGED, CHILD, RATING
	}

	enum MyLabels implements Label {
		INGREDIENT, RECIPE, TAG, USER
	}

//	@AfterClass
//	public void shutDown() {
//		CanonicalItemFactory.stopES();
//	}

	@AfterClass
	public void destroyTestDatabase() {
	    if ( bootstrapper != null) {
	    	bootstrapper.stop();
	    }

//	    graphDb.shutdown();
	}
}