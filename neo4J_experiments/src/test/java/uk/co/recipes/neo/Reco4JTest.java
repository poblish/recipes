package uk.co.recipes.neo;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.reco4j.recommender.RecommendersFactory.*;
import static org.reco4j.similarity.ISimilarityConfig.SIMILARITY_TYPE_COSINE;
import static org.reco4j.similarity.ISimilarityConfig.SIMILARITY_TYPE_JACCARD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.neo4j.graphdb.NotFoundException;
import org.reco4j.engine.RecommenderEngine;
import org.reco4j.graph.IEdgeType;
import org.reco4j.graph.INode;
import org.reco4j.graph.neo4j.Neo4JNode;
import org.reco4j.graph.neo4j.Neo4jGraph;
import org.reco4j.graph.neo4j.util.Neo4JPropertiesHandle;
import org.reco4j.model.Rating;
import org.reco4j.recommender.IRecommender;
import org.reco4j.similarity.BasicSimilarity;
import org.reco4j.similarity.ISimilarityConfig;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * 
 * See: http://www.reco4j.org/get-started.jsp
 * 
 * @author andrewregan
 * 
 */
public class Reco4JTest {

    private final static Supplier<List<List<Rating>>>  INGREDIENT_RATINGS_LAZY = new Supplier<List<List<Rating>>>() {

        @Override
        public List<List<Rating>> get() {
            return asList( asList( newRating( 11, 2.0), newRating( 36, 6.0), newRating( 43, 9.0), newRating( 22, 7.0)), asList( newRating( 48, 8.0), newRating( 11, 1.0), newRating( 36, 3.0), newRating( 22, 5.0)), asList( newRating( 40, 10.0), newRating( 44, 10.0), newRating( 43, 4.0)), asList( newRating( 40, 9.0), newRating( 48, 8.0), newRating( 43, 1.0)), asList( newRating( 18, 5.0) ));
        }};

    private final static Supplier<List<List<Rating>>>  RECIPE_RATINGS_LAZY = new Supplier<List<List<Rating>>>() {

        @Override
        public List<List<Rating>> get() {
            return asList( asList( newRating( 1, 8.0), newRating( 33, 7.0)), asList( newRating( 1, 6.0), newRating( 33, 3.0)), asList( newRating( 1, 10.0)), new ArrayList<Rating>(), asList( newRating( 33, 4.0)) );
        }};

    public static class OurSimilarity extends BasicSimilarity<ISimilarityConfig> {

        public OurSimilarity( final ISimilarityConfig config) {
            super(config);
        }

        @Override
        public double getSimilarity(INode x, INode y, IEdgeType edgeType) {
            return Math.random();
        }
    }

    @Test
    public void testKnn_OurSimilarity() throws IOException {

        Properties props = getBasicProperties();
        props.setProperty("SimilarityImplClassName", "uk.co.recipes.neo.Reco4JTest$OurSimilarity");
        props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_COLLABORATIVE);
        props.setProperty("rankEdgeIdentifier", "xxx");

        /* ingredientsTest( props, new RecommendationExpectations() {

            @Override public List<List<Rating>> expectedRecommendations() {
                return (List<List<Rating>>) asList( new ArrayList<Rating>(), new ArrayList<Rating>(), new ArrayList<Rating>(), new ArrayList<Rating>(), new ArrayList<Rating>() ) ;
            }
        } ); */
    }

    @Test
    public void testKnn_CosineSimilarity() throws IOException {

        Properties props = getBasicProperties();
        props.setProperty("DistanceAlgorithm", "" + SIMILARITY_TYPE_COSINE);
        props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_COLLABORATIVE);

        ingredientsTest( props, new RecommendationExpectations() {

            @Override public List<List<Rating>> expectedRecommendations() {
                return asList( asList( newRating( 44, 9.0), newRating( 40, 9.0), newRating( 48, 5.855755076535925)), asList( newRating( 40, 8.0), newRating( 43, 4.25)), asList( newRating( 48, 7.303061543300931), newRating( 11, 4.0), newRating( 36, 4.0), newRating( 22, 4.0)), asList( newRating( 44, 5.404082057734576), newRating( 11, 4.853571800517753), newRating( 36, 4.853571800517753), newRating( 22, 4.853571800517753)), new ArrayList<Rating>() ) ;
            }
        } );
    }

    @Test
    public void testRecipes_Knn_CosineSimilarity() throws IOException {

        Properties props = getRecipeProperties();
        props.setProperty("DistanceAlgorithm", "" + SIMILARITY_TYPE_COSINE);
        props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_COLLABORATIVE);

        recipesTest( props, new RecommendationExpectations() {

            @Override public List<List<Rating>> expectedRecommendations() {
                return asList( new ArrayList<Rating>(), new ArrayList<Rating>(), asList( newRating( 33, 10.0) ), new ArrayList<Rating>(), asList( newRating( 1, 4.0) ));
            }
        } );
    }

    @Test
    public void testKnn_JaccardSimilarity() throws IOException {

        final Properties props = getBasicProperties();
        props.setProperty("DistanceAlgorithm", "" + SIMILARITY_TYPE_JACCARD);
        props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_COLLABORATIVE);

        ingredientsTest( props, new RecommendationExpectations() {

            @Override public List<List<Rating>> expectedRecommendations() {
                return asList( asList( newRating( 44, 9.0), newRating( 40, 9.0), newRating( 48, 5.8)), asList( newRating( 40, 8.0), newRating( 43, 4.25)), asList( newRating( 48, 7.428571428571429), newRating( 11, 4.0), newRating( 36, 4.0), newRating( 22, 4.0)), asList( newRating( 44, 5.8), newRating( 11, 5.0), newRating( 36, 5.0), newRating( 22, 5.0)), new ArrayList<Rating>() ) ;
            }
        } );
    }
    
    @Test
    public void testMatrixFactorization() throws IOException {

        final Properties props = getBasicProperties();
        props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_MATRIXFACTORIZATION);

        ingredientsTest( props, new RecommendationExpectations() {

            @Override public List<List<Rating>> expectedRecommendations() {
                return asList( asList( newRating( 44, 5.0), newRating( 48, 5.0), newRating( 40, 5.0), newRating( 18, 2.4307454323366406), newRating( 39, 1.6410948618808914), newRating( 21, 1.6410948618808914), newRating( 38, 1.6410948618808914), newRating( 15, 1.6410948618808914), newRating( 10, 1.6410948618808914), newRating( 23, 1.6410948618808914)), asList( newRating( 44, 5.0), newRating( 40, 5.0), newRating( 43, 3.885659403453608), newRating( 18, 2.0350912702096733), newRating( 39, 1.460197953021539), newRating( 21, 1.460197953021539), newRating( 38, 1.460197953021539), newRating( 15, 1.460197953021539), newRating( 10, 1.460197953021539), newRating( 23, 1.460197953021539)), asList( newRating( 36, 5.0), newRating( 48, 5.0), newRating( 22, 5.0), newRating( 18, 3.1356174293457277), newRating( 11, 2.481449863575502), newRating( 39, 1.9452219912396973), newRating( 21, 1.9452219912396973), newRating( 38, 1.9452219912396973), newRating( 15, 1.9452219912396973), newRating( 10, 1.9452219912396973)), asList( newRating( 44, 5.0), newRating( 36, 5.0), newRating( 22, 5.0), newRating( 18, 2.4732085918800446), newRating( 11, 2.023403936091514), newRating( 39, 1.6485180854637778), newRating( 21, 1.6485180854637778), newRating( 38, 1.6485180854637778), newRating( 15, 1.6485180854637778), newRating( 10, 1.6485180854637778)), asList( newRating( 40, 3.1023068569187466), newRating( 44, 2.4441536069516387), newRating( 48, 2.3746534613519255), newRating( 22, 1.9680056996810968), newRating( 36, 1.7329214109297617), newRating( 43, 1.675841376681481), newRating( 11, 1.35161818659906), newRating( 39, 1.325687991999656), newRating( 21, 1.325687991999656), newRating( 38, 1.325687991999656)) ) ;
            }
        } );
    }

	@Test
	public void testMahout_LogLikelihoodSimilarity() throws IOException {

		Properties props = getBasicProperties();
		props.setProperty("recommenderType", "" + RECOMMENDER_TYPE_MAHOUT);

		ingredientsTest( props, new RecommendationExpectations() {

			@Override public List<List<Rating>> expectedRecommendations() {
				return asList( asList( newRating( 40, 9.0), newRating( 44, 9.0)), asList( newRating( 40, 8.0) ), asList( newRating( 48, 6.99491024017334), newRating( 22, 4.0), newRating( 11, 4.0), newRating( 36, 4.0)), asList( newRating( 44, 4.991103649139404) ), new ArrayList<Rating>() ) ;
			}
		} );
	}

    private static void ingredientsTest( final Properties inProps, final RecommendationExpectations inExpectations) throws IOException {
        basicTest( inProps, INGREDIENT_RATINGS_LAZY, inExpectations);
    }

    private static void recipesTest( final Properties inProps, final RecommendationExpectations inExpectations) throws IOException {
        basicTest( inProps, RECIPE_RATINGS_LAZY, inExpectations);
    }

	private static void basicTest( final Properties inProps, final Supplier<List<List<Rating>>> inActualRatings, final RecommendationExpectations inExpectations) throws IOException {
	    final Neo4JPropertiesHandle ph = Neo4JPropertiesHandle.getInstance();
        ph.setProperties(inProps);

        final Neo4jGraph graphDB = new Neo4jGraph(ph);

        try {
    		graphDB.initDatabase();
    
    		IRecommender<?> rec = RecommenderEngine.buildRecommender(graphDB, inProps);
    
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
    				gotRecommendedRatings.add( filterRatings( rec.recommend(node) ) );
    			}
    			catch (NotFoundException t) {
    				// Ignore
    			}
    		}
    
    		assertThat( gotNames, is( asList("User 1","User 2","User 3","User 4","User 5") ));
    		assertThat( gotActualRatings.size(), is( inActualRatings.get().size() ));
    		assertThat( gotActualRatings, is( inActualRatings.get() ));
    		assertThat( gotRecommendedRatings, is( inExpectations.expectedRecommendations() ));
        }
		finally {
		    if ( graphDB.getGraphDB() != null) {
		        graphDB.getGraphDB().shutdown();
		    }
		}
	}

	private interface RecommendationExpectations {
		List<List<Rating>> expectedRecommendations();
	}

    private Properties getBasicProperties() {
        Properties props = new Properties();
        props.setProperty("dbPath", "/private/tmp/neo4j");
        props.setProperty("recalculateSimilarity", "true");
        props.setProperty("userType", "USER");
        props.setProperty("userIdentifier", "name");
        props.setProperty("itemType", "INGREDIENT");
        props.setProperty("itemIdentifier", "name");
        props.setProperty("rankEdgeIdentifier", "RATING");
        props.setProperty("RankValueIdentifier", "score");
        return props;
    }

    private Properties getRecipeProperties() {
        Properties props = new Properties();
        props.setProperty("dbPath", "/private/tmp/neo4j");
        props.setProperty("recalculateSimilarity", "true");
        props.setProperty("userType", "USER");
        props.setProperty("userIdentifier", "name");
        props.setProperty("itemType", "RECIPE");
        props.setProperty("itemIdentifier", "name");
        props.setProperty("rankEdgeIdentifier", "RECIPE_RATING");
        props.setProperty("RankValueIdentifier", "score");
        return props;
    }

	private static Rating newRating( final long inNodeId, final double inScore) {
		return new Rating( new Neo4JNode(inNodeId), inScore);
	}

	private static List<Rating> filterRatings( final List<Rating> inRatings) {
		return FluentIterable.from(inRatings).filter( new Predicate<Rating>() {
			public boolean apply( final Rating r) {
				return r.getRate() >= 1.0;  // Cuts down the field
			}
		} ).toList();
	}
}