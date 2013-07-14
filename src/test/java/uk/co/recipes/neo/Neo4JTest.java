package uk.co.recipes.neo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
	    graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
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
		assertThat( (String) foundNode.getProperty( "name" ), is( "Nancy" ) );	}

	@AfterClass
	public void destroyTestDatabase() {
	    graphDb.shutdown();
	}
}