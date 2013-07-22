package uk.co.recipes.neo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.recipes.TestDataUtils.parseIngredientsFrom;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.server.Bootstrapper;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
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
	private Bootstrapper bootstrapper = null;

	@BeforeClass
	public void prepareTestDatabase() {
	    graphDb = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
	    										.setConfig( GraphDatabaseSettings.node_keys_indexable, "name")
	    										.setConfig( GraphDatabaseSettings.node_auto_indexing, "true")
	    										.newGraphDatabase();

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
			recipeNode.setProperty( "name", "Cashew Curry");

			final List<IIngredient> ings = parseIngredientsFrom("chCashBlackSpiceCurry.txt");

			for ( IIngredient each : ings) {
				Optional<Node> on = findItem( each.getItem() );
				Node n = on.isPresent() ? on.get() : createItemHierarchy( each.getItem() );

				n.createRelationshipTo( recipeNode, MyRelationshipTypes.CONTAINED_IN);

				handleTagsForIIngredient( each, n);
			}
//
//			RecipeStage rs = new RecipeStage();
//			rs.addIngredients(ings);
//			r.addStage(rs);

			final Node foundNode = findItem("Cumin Seeds").get();
			System.out.println("Props " + Lists.newArrayList( foundNode.getPropertyKeys() ));
			System.out.println("Relns " + Lists.newArrayList( foundNode.getRelationships() ));
			System.out.println(recipeNode);

			final Node foundRecipe = findRecipe("Cashew Curry").get();
			System.out.println("Props " + Lists.newArrayList( foundRecipe.getPropertyKeys() ));
			System.out.println("Relns " + Lists.newArrayList( foundRecipe.getRelationships() ));

			///////////////////////////////////////////////////////////////////////////////////////////

			Node recipeNode2 = graphDb.createNode( MyLabels.RECIPE );
			recipeNode2.setProperty( "name", "Thai Fish Curry");

			final List<IIngredient> ings2 = parseIngredientsFrom("ttFishCurry.txt");

			for ( IIngredient each : ings2) {
				Optional<Node> on = findItem( each.getItem() );
				Node n = on.isPresent() ? on.get() : createItemHierarchy( each.getItem() );

				n.createRelationshipTo( recipeNode2, MyRelationshipTypes.CONTAINED_IN);

                handleTagsForIIngredient( each, n);
			}

			///////////////////////////////////////////////////////////////////////////////////////////  Collaborative Filtering. Follow our existing Taste example: 1,1,7; 1,2,9; 1,3,6; 1,4,2; 2,1,5; 2,3,3; 2,4,1; 2,5,8; 3,2,4; 3,6,10; 3,7,10; 4,2,1; 4,5,8; 4,7,9; 5,8,5
			///////////////////////////////////////////////////////////////////////////////////////////  Where Ingredient indexes are for: Cumin Seeds; Green Beans; Turmeric; Garlic Cloves; Basmati Rice; Tamarind Paste; Fennel Seed; Coriander

			final Node user_1 = createUser("User 1");
			rateItem( user_1, "Cumin Seeds", 7);
			rateItem( user_1, "Green Beans", 9);
			rateItem( user_1, "Turmeric", 6);
			rateItem( user_1, "Garlic Cloves", 2);

			final Node user_2 = createUser("User 2");
			rateItem( user_2, "Cumin Seeds", 5);
			rateItem( user_2, "Turmeric", 3);
			rateItem( user_2, "Garlic Cloves", 1);
			rateItem( user_2, "Basmati Rice", 8);

			final Node user_3 = createUser("User 3");
			rateItem( user_3, "Green Beans", 4);
			rateItem( user_3, "Tamarind Paste", 10);
			rateItem( user_3, "Fennel Seed", 10);

			final Node user_4 = createUser("User 4");
			rateItem( user_4, "Green Beans", 1);
			rateItem( user_4, "Basmati Rice", 8);
			rateItem( user_4, "Fennel Seed", 9);

			final Node user_5 = createUser("User 5");
			rateItem( user_5, "Coriander", 5);

//			tx.success();

			///////////////////////////////////////////////////////////////////////////////////////////  See: http://docs.neo4j.org/chunked/stable/tutorials-cypher-java.html

			final ExecutionEngine engine = new ExecutionEngine(graphDb);

			ExecutionResult result1 = engine.execute("START me=node:node_auto_index(name='Cashew Curry') RETURN me");
			System.out.println("Find with node:node_auto_index = \r" + result1.dumpToString());
			ExecutionResult result2 = engine.execute("START n=node(*) WHERE n.name ! = 'Cumin Seeds' RETURN n, n.name");
			System.out.println("Find by node name = \r" + result2.dumpToString());

			ExecutionResult iForR = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds WHERE NOT(me=ingreds) RETURN ingreds.name as name, COUNT(*) as c ORDER BY c DESC");
			System.out.println("Ingredients for Recipe = \r" + iForR.dumpToString());

			ExecutionResult tForR1 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds<-[:TAGGED]-tag RETURN tag.name as name, COUNT(*) as c ORDER BY c DESC, name");
			System.out.println("Tags for Recipe 1 = \r" + tForR1.dumpToString());

			ExecutionResult tForR2 = engine.execute("START me=node:node_auto_index(name='Cashew Curry') MATCH me<-[:CONTAINED_IN]-ingreds<-[:TAGGED]-tag RETURN tag.name as name, COUNT(*) as c ORDER BY c DESC, name");
			System.out.println("Tags for Recipe 2 = \r" + tForR2.dumpToString());

			ExecutionResult result4 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds<-[:TAGGED]-tag-[:TAGGED]->otherIngredients-[:CONTAINED_IN]->others WHERE NOT(me=others) RETURN others.name as name, tag.name, COUNT(*) as c ORDER BY c DESC, name");
			System.out.println("Tags shared with other Recipes??? \r" + result4.dumpToString());

			ExecutionResult result5 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds-[:CONTAINED_IN]->others WHERE NOT(me=others) RETURN others.name as name, ingreds.name ORDER BY name, ingreds.name");
			System.out.println("Ingredients shared with other Recipes \r" + result5.dumpToString());

			ExecutionResult result6 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-ingreds-[:CONTAINED_IN]->others WHERE NOT(me=others) RETURN others.name as name, COUNT(*) AS c ORDER BY c DESC, name");
			System.out.println("Ingredients shared with other Recipes II \r" + result6.dumpToString());

			ExecutionResult result7 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-allMine, allTheirs-[:CONTAINED_IN]->others WHERE NOT(me=others) and NOT(allMine=allTheirs) RETURN others.name as name, COUNT(DISTINCT allMine) + COUNT(DISTINCT allTheirs) AS num_unshared ORDER BY name, num_unshared DESC");
			System.out.println("Ingredients not shared with other Recipes \r" + result7.dumpToString());

//			ExecutionResult result8 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:TAGGED]-allMine, allTheirs-[:TAGGED]->others WHERE NOT(me=others) and NOT(allMine=allTheirs) RETURN others.name as name, COUNT(DISTINCT allMine) + COUNT(DISTINCT allTheirs) AS num_unshared ORDER BY name, num_unshared DESC");
//			System.out.println("Tags not shared with other Recipes?? \r" + result8.dumpToString());

            ExecutionResult tForI = engine.execute("START me=node:node_auto_index(name='Ginger') MATCH me<-[:TAGGED]-tags WHERE NOT(me=tags) RETURN tags.name as name, COUNT(*) as c ORDER BY c DESC");
            System.out.println("Tags for Ingredient = \r" + tForI.dumpToString());

            ExecutionResult iCountForT = engine.execute("START me=node:node_auto_index(name='INDIAN') MATCH me-[:TAGGED]->indianIngr RETURN DISTINCT indianIngr.name as name ORDER BY name");  // Not too happy with DISTINCT, but works...
            System.out.println("Ingredients tagged 'INDIAN' = \r" + iCountForT.dumpToString());

            ExecutionResult iCountForTT = engine.execute("START a=node:node_auto_index(name='INDIAN'), b=node:node_auto_index(name='SPICE')" +
                            " MATCH a-[:TAGGED]->indianIngr, b-[:TAGGED]->spiceIngr WHERE indianIngr=spiceIngr RETURN DISTINCT indianIngr.name as name ORDER BY name");  // Bit crazy? Not too happy with DISTINCT, but works...
            System.out.println("Ingredients tagged 'INDIAN' and 'SPICE' = \r" + iCountForTT.dumpToString());

            ExecutionResult rCountForT = engine.execute("START me=node:node_auto_index(name='INDIAN') MATCH me-[:TAGGED]->ingredients-[:CONTAINED_IN]->recipe RETURN recipe.name AS name, COUNT(*) AS num_ingredients ORDER BY num_ingredients DESC, name");
            System.out.println("Recipes tagged 'INDIAN' = \r" + rCountForT.dumpToString());

            ExecutionResult parent1Level = engine.execute("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD]->parent RETURN parent.name AS name ORDER BY name");
            System.out.println("Oil parentage I = \r" + parent1Level.dumpToString());

            ExecutionResult parent2Level = engine.execute("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD]->parent-[:CHILD]->parent2 RETURN parent2.name AS name ORDER BY name");
            System.out.println("Oil parentage II = \r" + parent2Level.dumpToString());

            ExecutionResult parentTopLevel = engine.execute("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD*1..]->parent WHERE NOT(parent-[:CHILD]->()) RETURN parent.name AS name ORDER BY name");
            System.out.println("Oil top parentage = \r" + parentTopLevel.dumpToString());
		}
		catch ( Exception e) {
			tx.failure();
			throw e;
		}
		finally {
			tx.finish();
		}
	}

	private Node createUser( final String inName) {
		final Node n = graphDb.createNode( MyLabels.USER );
		n.setProperty( "name", inName);
		n.setProperty( "type", "USER");
		return n;
	}

	private void rateItem( final Node inUser, final String inItemName, final int inScore) {
		inUser.createRelationshipTo( findItem(inItemName).get(), MyRelationshipTypes.RATING).setProperty( "score", inScore);
	}

    private void handleTagsForIIngredient( final IIngredient inIngr, final Node inIngrNode) {
        for ( Entry<ITag,Serializable> eachTag : inIngr.getItem().getTags().entrySet()) {
            Optional<Node> optTagNode = findTag( eachTag.getKey() );
            Node tagNode = optTagNode.isPresent() ? optTagNode.get() : graphDb.createNode( MyLabels.TAG );

            tagNode.setProperty( "name", eachTag.getKey().toString());  // FIXME Don't overwrite!
            // FIXME Save value too, surely?
            tagNode.createRelationshipTo( inIngrNode, MyRelationshipTypes.TAGGED);
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

    private Optional<Node> findTag( final ITag inName) {
        final ResourceIterator<Node> itr = graphDb.findNodesByLabelAndProperty( MyLabels.TAG, "name", /* FIXME? */ inName.toString()).iterator();

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

	private Node createItemHierarchy( final ICanonicalItem inItem) {
		final Node us = graphDb.createNode( MyLabels.INGREDIENT );
		us.setProperty( "name", inItem.getCanonicalName());

		ICanonicalItem childItem = inItem;
		Node childNode = us;
		while (childItem.parent().isPresent()) {
			final Optional<Node> on = findItem( childItem.parent().get() );
			Node parentNode = on.isPresent() ? on.get() : createItemHierarchy( childItem.parent().get() );

			// Make sure we haven't been here before
			if ( childNode.getSingleRelationship( MyRelationshipTypes.CHILD, Direction.OUTGOING) != null) {
				break;
			}

			System.out.println("--> " + childItem + " is child of " + childItem.parent());
			childNode.createRelationshipTo( parentNode, MyRelationshipTypes.CHILD);
			
			childItem = childItem.parent().get();
			childNode = parentNode;
		}

		return us;
	}

	enum MyRelationshipTypes implements RelationshipType {
		CONTAINED_IN, TAGGED, CHILD, RATING
	}

	enum MyLabels implements Label {
		INGREDIENT, RECIPE, TAG, USER
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

			// The node should have an id greater than 0, which is the id of the
			// reference node.
//			assertThat( n.getId(), is( greaterThan( 0L ) ) );

			// Retrieve a node by using the id of the created node. The id's and
			// property should match.
			Node foundNode = graphDb.getNodeById( n.getId() );
			assertThat( foundNode.getId(), is( n.getId() ) );
			assertThat( (String) foundNode.getProperty( "name" ), is( "Nancy" ) );
		}
		catch ( Exception e ) {
			tx.failure();
		}
		finally {
			tx.finish();
		}
	}

	@AfterClass
	public void shutDown() {
		CanonicalItemFactory.stopES();
	}

	@AfterClass
	public void destroyTestDatabase() {
	    if ( bootstrapper != null) {
	    	bootstrapper.stop();
	    }

	    graphDb.shutdown();
	}
}