package uk.co.recipes.taste;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.AbstractItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.TestDataUtils;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.ItemsLoader;
import uk.co.recipes.persistence.RecipeFactory;
import uk.co.recipes.similarity.IncompatibleIngredientsException;
import uk.co.recipes.similarity.Similarity;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class MahoutSimilarityTest {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );

	private EsItemFactory itemFactory = GRAPH.get( EsItemFactory.class );
	private RecipeFactory recipeFactory = GRAPH.get( RecipeFactory.class );
	private TestDataUtils dataUtils = GRAPH.get( TestDataUtils.class );

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

	@Test
	public void testCustomSimilarity() throws IOException, TasteException {
		final FileDataModel model = new FileDataModel( new File("src/test/resources/taste/main.txt") );
		
		final ItemSimilarity similarity = new MySimilarity(model); // GenericItemSimilarity( Lists.newArrayList( s1, s2, s3, s56) );

		final Recommender recommender = new GenericItemBasedRecommender( model, similarity);
		long userId = 1000L;
		assertThat( recommender.recommend(userId++, 10).toString(), is("[RecommendedItem[item:7, value:6.3762536], RecommendedItem[item:6, value:6.235465], RecommendedItem[item:5, value:5.838157]]"));
		assertThat( recommender.recommend(userId++, 10).toString(), is("[RecommendedItem[item:2, value:4.2387347], RecommendedItem[item:7, value:4.135262]]"));
		assertThat( recommender.recommend(userId++, 10).toString(), is("[RecommendedItem[item:1, value:7.887853], RecommendedItem[item:4, value:7.791888], RecommendedItem[item:3, value:7.778363], RecommendedItem[item:5, value:7.7400393]]"));
		assertThat( recommender.recommend(userId++, 10).toString(), is("[RecommendedItem[item:1, value:6.0914598], RecommendedItem[item:4, value:6.060889], RecommendedItem[item:6, value:5.7682915], RecommendedItem[item:3, value:5.7291117]]"));
		assertThat( recommender.recommend(userId++, 10).toString(), is("[]"));
	}

	private class MySimilarity extends AbstractItemSimilarity {

		protected MySimilarity(DataModel dataModel) {
			super(dataModel);
		}

		/* (non-Javadoc)
		 * @see org.apache.mahout.cf.taste.similarity.ItemSimilarity#itemSimilarity(long, long)
		 */
		@Override
		public double itemSimilarity( long id1, long id2) throws TasteException {
			try {
				return Similarity.amongIngredients( getItem(id1), getItem(id2));
			} catch (IncompatibleIngredientsException e) {
				return 0;
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.mahout.cf.taste.similarity.ItemSimilarity#itemSimilarities(long, long[])
		 */
		@Override
		public double[] itemSimilarities( long id1, long[] otherIds) throws TasteException {
			final Collection<Double> sims = Lists.newArrayList();

			try {
				final List<IIngredient> us = getItem(id1);
	
				for ( long eachOther : otherIds) {
					try {
						sims.add( Similarity.amongIngredients( us, getItem(eachOther)) );
					} catch (IncompatibleIngredientsException e) {
						sims.add( Double.NaN );
					} catch (IOException e) {
						Throwables.propagate(e);
					}
				}

				return Doubles.toArray(sims);
			}
			catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	private List<IIngredient> getItem( long inId) throws IOException {
		switch ((int) inId) {
			case 0:
				return dataUtils.parseIngredientsFrom("inputs.txt");
			case 1:
				return dataUtils.parseIngredientsFrom("inputs2.txt");
			case 2:
				return dataUtils.parseIngredientsFrom("inputs3.txt");
			case 3:
				return dataUtils.parseIngredientsFrom("chCashBlackSpiceCurry.txt");
			case 4:
				return dataUtils.parseIngredientsFrom("bol1.txt");
			case 5:
				return dataUtils.parseIngredientsFrom("bol2.txt");
			case 6:
				return dataUtils.parseIngredientsFrom("chineseBeef.txt");
			case 7:
				return dataUtils.parseIngredientsFrom("ttFishCurry.txt");
		}
		
		return null;
	}

	@AfterClass
	public void shutDown() {
		itemFactory.stopES();
	}
}