package uk.co.recipes.neo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IItemPersistence;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.client.Client;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.server.Bootstrapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.test.TestDataUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Neo4JTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private Client esClient = GRAPH.get( Client.class );
	private IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
	private IRecipePersistence recipeFactory = GRAPH.get( EsRecipeFactory.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

	private GraphDatabaseService graphDb;
	private Bootstrapper bootstrapper = null;

	@BeforeClass
	public void prepareTestDatabase() throws FileNotFoundException {
		FileUtils.deleteRecursive( new File("/private/tmp/neo4j") );  // Yuk!

		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder("/private/tmp/neo4j")
	    										.setConfig( GraphDatabaseSettings.node_keys_indexable, "name")
	    										.setConfig( GraphDatabaseSettings.node_auto_indexing, "true")
	    										.newGraphDatabase();
	}

	@BeforeClass
	public void cleanIndices() throws ClientProtocolException, IOException {
		itemFactory.deleteAll();
		recipeFactory.deleteAll();
	}

	@BeforeClass
	public void loadIngredientsFromYaml() throws InterruptedException, IOException {
		GRAPH.get( ItemsLoader.class ).load();
		Thread.sleep(1000);
	}

	@Test(groups="before")
	public void recipesTest() throws IOException {
		Transaction tx = graphDb.beginTx();

		try {
//			Recipe r = new Recipe("Lamb Cobbler");
			Node recipeNode = createRecipe("Cashew Curry");

			final List<IIngredient> ings = dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");

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

			Node recipeNode2 = createRecipe("Thai Fish Curry");

			final List<IIngredient> ings2 = dataUtils.parseIngredientsFrom("ttFishCurry.txt");

			for ( IIngredient each : ings2) {
				Optional<Node> on = findItem( each.getItem() );
				Node n = on.isPresent() ? on.get() : createItemHierarchy( each.getItem() );

				n.createRelationshipTo( recipeNode2, MyRelationshipTypes.CONTAINED_IN);

                handleTagsForIIngredient( each, n);
			}

			////////////////////////////////////////////////////////////////////  Collaborative Filtering. Follow our existing Taste example: 1,1,7; 1,2,9; 1,3,6; 1,4,2; 2,1,5; 2,3,3; 2,4,1; 2,5,8; 3,2,4; 3,6,10; 3,7,10; 4,2,1; 4,5,8; 4,7,9; 5,8,5
			////////////////////////////////////////////////////////////////////  Where Ingredient indexes are for: (1) Cumin Seeds; (2) Green Beans; (3) Turmeric; (4) Garlic Cloves; (5) Basmati Rice; (6) Tamarind Paste; (7) Fennel Seed; (8) Coriander

			final Node user_1 = createUser("User 1");
			rateItem( user_1, "Cumin Seeds", 7);
			rateItem( user_1, "Green Beans", 9);
			rateItem( user_1, "Turmeric", 6);
			rateItem( user_1, "Garlic Cloves", 2);
			rateItem( user_1, "Thai Fish Curry", 7);  // Recipe
			rateItem( user_1, "Cashew Curry", 8);  // Recipe

			final Node user_2 = createUser("User 2");
			rateItem( user_2, "Cumin Seeds", 5);
			rateItem( user_2, "Turmeric", 3);
			rateItem( user_2, "Garlic Cloves", 1);
			rateItem( user_2, "Basmati Rice", 8);
			rateItem( user_2, "Thai Fish Curry", 3);  // Recipe
			rateItem( user_2, "Cashew Curry", 6);  // Recipe

			final Node user_3 = createUser("User 3");
			rateItem( user_3, "Green Beans", 4);
			rateItem( user_3, "Tamarind Paste", 10);
			rateItem( user_3, "Fennel Seed", 10);
			rateItem( user_3, "Cashew Curry", 10);  // Recipe

			final Node user_4 = createUser("User 4");
			rateItem( user_4, "Green Beans", 1);
			rateItem( user_4, "Basmati Rice", 8);
			rateItem( user_4, "Fennel Seed", 9);

			final Node user_5 = createUser("User 5");
			rateItem( user_5, "Coriander", 5);
			rateItem( user_5, "Thai Fish Curry", 4);  // Recipe

			tx.success();  // *Needed* for Reco4J

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

			ExecutionResult result7 = engine.profile("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:CONTAINED_IN]-allMine, allTheirs-[:CONTAINED_IN]->others WHERE NOT(me=others) and NOT(allMine=allTheirs) RETURN others.name as name, COUNT(DISTINCT allMine) + COUNT(DISTINCT allTheirs) AS num_unshared ORDER BY name, num_unshared DESC");
			System.out.println("Ingredients not shared with other Recipes \r" + result7.dumpToString() + "\r" + result7.executionPlanDescription());

//			ExecutionResult result8 = engine.execute("START me=node:node_auto_index(name='Thai Fish Curry') MATCH me<-[:TAGGED]-allMine, allTheirs-[:TAGGED]->others WHERE NOT(me=others) and NOT(allMine=allTheirs) RETURN others.name as name, COUNT(DISTINCT allMine) + COUNT(DISTINCT allTheirs) AS num_unshared ORDER BY name, num_unshared DESC");
//			System.out.println("Tags not shared with other Recipes?? \r" + result8.dumpToString());

            ExecutionResult tForI = engine.execute("START me=node:node_auto_index(name='Ginger') MATCH me<-[:TAGGED]-tags WHERE NOT(me=tags) RETURN tags.name as name, COUNT(*) as c ORDER BY c DESC");
            System.out.println("Tags for Ingredient = \r" + tForI.dumpToString());

            ExecutionResult iCountForT = engine.profile("START me=node:node_auto_index(name='INDIAN') MATCH me-[:TAGGED]->indianIngr RETURN DISTINCT indianIngr.name as name ORDER BY name");  // Not too happy with DISTINCT, but works...
            System.out.println("Ingredients tagged 'INDIAN' = \r" + iCountForT.dumpToString() + "\r" + iCountForT.executionPlanDescription());

            ExecutionResult iCountForTT = engine.profile("START a=node:node_auto_index(name='INDIAN'), b=node:node_auto_index(name='SPICE')" +
                            " MATCH a-[:TAGGED]->indianIngr, b-[:TAGGED]->spiceIngr WHERE indianIngr=spiceIngr RETURN DISTINCT indianIngr.name as name ORDER BY name");  // Bit crazy? Not too happy with DISTINCT, but works...
            System.out.println("Ingredients tagged 'INDIAN' and 'SPICE' = \r" + iCountForTT.dumpToString() + "\r" + iCountForTT.executionPlanDescription());

            ExecutionResult rCountForT = engine.profile("START me=node:node_auto_index(name='INDIAN') MATCH me-[:TAGGED]->ingredients-[:CONTAINED_IN]->recipe RETURN recipe.name AS name, COUNT(*) AS num_ingredients ORDER BY num_ingredients DESC, name");
            System.out.println("Recipes tagged 'INDIAN' = \r" + rCountForT.dumpToString() + "\r" + rCountForT.executionPlanDescription());

            ExecutionResult parent1Level = engine.execute("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD]->parent RETURN parent.name AS name ORDER BY name");
            System.out.println("Oil parentage I = \r" + parent1Level.dumpToString());

            ExecutionResult parent2Level = engine.execute("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD]->parent-[:CHILD]->parent2 RETURN parent2.name AS name ORDER BY name");
            System.out.println("Oil parentage II = \r" + parent2Level.dumpToString());

            ExecutionResult parentTopLevel = engine.profile("START me=node:node_auto_index(name='Rapeseed Oil') MATCH me-[:CHILD*1..]->parent WHERE NOT(parent-[:CHILD]->()) RETURN parent.name AS name ORDER BY name");
            System.out.println("Oil top parentage = \r" + parentTopLevel.dumpToString() + "\r" + parentTopLevel.executionPlanDescription());
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

	private Node createRecipe( final String inName) {
		final Node n = graphDb.createNode( MyLabels.RECIPE );
		n.setProperty( "name", inName);
		n.setProperty( "type", "RECIPE");
		return n;
	}

	private Node createTag( final ITag inTag, final Serializable inValue) {
		final Node n = graphDb.createNode( MyLabels.TAG );
		n.setProperty( "name", inTag.toString());
		n.setProperty( "type", "TAG");
        // FIXME Save value too, surely?
		return n;
	}

	private void rateItem( final Node inUser, final String inItemName, final int inScore) {
		Optional<Node> gotItem = findItem(inItemName);
		if (gotItem.isPresent()) {
			inUser.createRelationshipTo( gotItem.get(), MyRelationshipTypes.RATING).setProperty( "score", inScore);
		}
		else {
			inUser.createRelationshipTo( findRecipe(inItemName).get(), MyRelationshipTypes.RECIPE_RATING).setProperty( "score", inScore);
		}
	}

    private void handleTagsForIIngredient( final IIngredient inIngr, final Node inIngrNode) {
        for ( Entry<ITag,Serializable> eachTag : inIngr.getItem().getTags().entrySet()) {
            Optional<Node> optTagNode = findTag( eachTag.getKey() );
            Node tagNode = optTagNode.isPresent() ? optTagNode.get() : createTag( eachTag.getKey(), eachTag.getValue());

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
		us.setProperty( "type", "INGREDIENT");

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
		CONTAINED_IN, TAGGED, CHILD, RATING, RECIPE_RATING
	}

	enum MyLabels implements Label {
		INGREDIENT, RECIPE, TAG, USER
	}

	@Test(dependsOnGroups="before")
	public void basicTest() {
		Transaction tx = graphDb.beginTx();

		Node n = null;
		try
		{
			n = graphDb.createNode();
			n.setProperty( "name", "Nancy" );
			n.setProperty( "type", "");
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
		esClient.close();
	}

	@AfterClass
	public void destroyTestDatabase() {
	    if ( bootstrapper != null) {
	    	bootstrapper.stop();
	    }

	    graphDb.shutdown();
	}
}