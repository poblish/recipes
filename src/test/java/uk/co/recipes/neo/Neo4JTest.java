package uk.co.recipes.neo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.persistence.CanonicalItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Neo4JTest {

	private GraphDatabaseService graphDb;

	@BeforeClass
	public void prepareTestDatabase() {
	    graphDb = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
	    										.setConfig( GraphDatabaseSettings.node_keys_indexable, "name")
	    										.setConfig( GraphDatabaseSettings.node_auto_indexing, "true")
	    										.newGraphDatabase();
	}
	
	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		CanonicalItemFactory.startES();
		CanonicalItemFactory.deleteAll();
		RecipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		ItemsLoader.load();
		Thread.sleep(1000);
	}

	@Test
	public void recipesTest() throws IOException {
		Transaction tx = graphDb.beginTx();

		try {
//			Recipe r = new Recipe("Lamb Cobbler");
			Node recipeNode = graphDb.createNode( MyLabels.RECIPE );
			recipeNode.setProperty( "name", "Lamb Cobbler");

			final List<IIngredient> ings = parseIngredientsFrom("inputs.txt");

			for ( IIngredient each : ings) {
				Optional<Node> on = findItem( each.getItem() );
				Node n = on.isPresent() ? on.get() : graphDb.createNode( MyLabels.INGREDIENT );

				n.setProperty( "name", each.getItem().getCanonicalName());
				n.createRelationshipTo( recipeNode, MyRelationshipTypes.CONTAINED_IN);
			}
//
//			RecipeStage rs = new RecipeStage();
//			rs.addIngredients(ings);
//			r.addStage(rs);

			System.out.println("Got   " + graphDb.getNodeById(1L));

			final Node foundNode = findItem("Bay Leaf").get();
			System.out.println("Props " + Lists.newArrayList( foundNode.getPropertyKeys() ));
			System.out.println("Relns " + Lists.newArrayList( foundNode.getRelationships() ));
			System.out.println(recipeNode);

			final Node foundRecipe = findRecipe("Lamb Cobbler").get();
			System.out.println("Props " + Lists.newArrayList( foundRecipe.getPropertyKeys() ));
			System.out.println("Relns " + Lists.newArrayList( foundRecipe.getRelationships() ));

			///////////////////////////////////////////////////////////////////////////////////////////

			Node recipeNode2 = graphDb.createNode( MyLabels.RECIPE );
			recipeNode2.setProperty( "name", "Thai Fish Curry");

			final List<IIngredient> ings2 = parseIngredientsFrom("ttFishCurry.txt");

			for ( IIngredient each : ings2) {
				Optional<Node> on = findItem( each.getItem() );
				Node n = on.isPresent() ? on.get() : graphDb.createNode( MyLabels.INGREDIENT );

				n.setProperty( "name", each.getItem().getCanonicalName());
				n.createRelationshipTo( recipeNode2, MyRelationshipTypes.CONTAINED_IN);
			}

			///////////////////////////////////////////////////////////////////////////////////////////  See: http://docs.neo4j.org/chunked/stable/tutorials-cypher-java.html

			ExecutionEngine engine = new ExecutionEngine(graphDb);

			System.out.println("Result = " + engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds RETURN me,ingreds").dumpToString());

			ExecutionResult result = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry')" +
//						" MATCH me-[:CONTAINED_IN]->myFavorites-[:tagged]->tag<-[:tagged]-theirFavorites<-[:CONTAINED_IN]-people" + " WHERE NOT(me=people)" + " RETURN people.name as name, COUNT(*) as similar_favs" + " ORDER BY similar_favs DESC");
							" MATCH me<-[:CONTAINED_IN]-ingreds" + " WHERE NOT(me=ingreds)" + " RETURN ingreds.name as name, COUNT(*) as similar_favs" + " ORDER BY similar_favs DESC");
			System.out.println("Result = " + result.dumpToString());

			ExecutionResult result1 = engine.execute("START me=node:node_auto_index(name='Lamb Cobbler') RETURN me");
			System.out.println("Result1 = " + result1.dumpToString());
			ExecutionResult result2 = engine.execute("START n=node(*) WHERE n.name ! = 'Bay Leaf' RETURN n, n.name");
			System.out.println("Result2 = " + result2.dumpToString());
		}
		catch ( Exception e) {
			tx.failure();
			throw e;
		}
		finally {
			tx.finish();
		}
	}

	private Optional<Node> findItem( final ICanonicalItem inItem) {
		return findItem( inItem.getCanonicalName() );
	}

	private Optional<Node> findItem( final String inCanonicalName) {
		final ResourceIterator<Node> itr = graphDb.findNodesByLabelAndProperty( MyLabels.INGREDIENT, "name", inCanonicalName).iterator();

		try {
		    if (itr.hasNext()) {
		    	return Optional.fromNullable( itr.next() );
		    }

		    return Optional.absent();
		}
		finally {
			itr.close();
		}
	}

	private Optional<Node> findRecipe( final String inName) {
		final ResourceIterator<Node> itr = graphDb.findNodesByLabelAndProperty( MyLabels.RECIPE, "name", inName).iterator();

		try {
		    if (itr.hasNext()) {
		    	return Optional.fromNullable( itr.next() );
		    }

		    return Optional.absent();
		}
		finally {
			itr.close();
		}
	}

	enum MyRelationshipTypes implements RelationshipType {
		CONTAINED_IN
	}

	enum MyLabels implements Label {
		INGREDIENT, RECIPE
	}

	@Test
	public void basicTest() {
		Transaction tx = graphDb.beginTx();

		Node n = null;
		try
		{
			n = graphDb.createNode();
			n.setProperty( "name", "Nancy" );
			tx.success();
		}
		catch ( Exception e )
		{
			tx.failure();
		}
		finally
		{
			tx.finish();
		}

		// The node should have an id greater than 0, which is the id of the
		// reference node.
		assertThat( n.getId(), is( greaterThan( 0L ) ) );

		// Retrieve a node by using the id of the created node. The id's and
		// property should match.
		Node foundNode = graphDb.getNodeById( n.getId() );
		assertThat( foundNode.getId(), is( n.getId() ) );
		assertThat( (String) foundNode.getProperty( "name" ), is( "Nancy" ) );
	}

	@AfterClass
	public void shutDown() {
		CanonicalItemFactory.stopES();
	}

	@AfterClass
	public void destroyTestDatabase() {
	    graphDb.shutdown();
	}
}