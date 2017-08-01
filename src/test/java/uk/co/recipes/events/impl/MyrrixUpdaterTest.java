/**
 * 
 */
package uk.co.recipes.events.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.*;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.Units;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.mocks.MockFactories;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IIngredientQuantityScoreBooster;
import uk.co.recipes.service.impl.DefaultIngredientQuantityScoreBooster;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.NationalCuisineTags;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MyrrixUpdaterTest {

    @Inject MyrrixUpdater updater;

    private final static Optional<ICanonicalItem> ABSENT = Optional.absent();

    private static int COUNT = 0;

    @BeforeClass
    private void injectDependencies() {
        DaggerMyrrixUpdaterTest_TestComponent.builder().testModule( new TestModule() ).build().inject(this);
    }

    @Test
    public void testAddItem() {
        final Map<ITag,Serializable> tags = Maps.newHashMap();
        tags.put( CommonTags.SPICE, Boolean.TRUE);
        tags.put( NationalCuisineTags.INDIAN, "3.0");  // Try boosting

        final ICanonicalItem item = mock( ICanonicalItem.class );
        when( item.getCanonicalName() ).thenReturn("ginger");
        when( item.getTags() ).thenReturn(tags);
        when( item.parent() ).thenReturn(ABSENT);

        /////////////////////////////////////////////////

        assertThat( COUNT, is(0));

        updater.onAddItem( new AddItemEvent(item) );

        assertThat( COUNT, is(3)); // Parent tag + two Tags
    }

    @Test
    public void testAddRecipe() {
        final Map<ITag,Serializable> tags = Maps.newHashMap();
        tags.put( CommonTags.SPICE, Boolean.TRUE);
        tags.put( NationalCuisineTags.INDIAN, "3.0");  // Try boosting

        final ICanonicalItem item = mock( ICanonicalItem.class );
        when( item.getCanonicalName() ).thenReturn("ginger");
        when( item.getTags() ).thenReturn(tags);
        when( item.parent() ).thenReturn(ABSENT);

        final User user = new User( "aregan", "Andrew R");

        final RecipeStage rs1 = new RecipeStage();
        rs1.addIngredients( new Ingredient( item, new Quantity( Units.TSP, 1)) );

        final Recipe r1 = new Recipe(user, "1", Locale.UK);
        r1.addStage(rs1);

        /////////////////////////////////////////////////

        assertThat( COUNT, is(3));

        updater.onAddRecipe( new AddRecipeEvent(r1) );

        assertThat( COUNT, is(6)); // Parent tag + two Tags
    }

    // FIXME Suboptimal: https://google.github.io/dagger/testing.html
    @Module
    public class TestModule extends DaggerModule {
        @Provides
        @Singleton
        EsItemFactory provideItemFactory() {
            return MockFactories.inMemoryItemFactory();
        }

        @Provides
        @Singleton
        IEventService provideEventService() {
            return new DefaultEventService();
        }

        @Provides
        @Singleton
        IIngredientQuantityScoreBooster provideIngredientQuantityScoreBooster() {
            return mock(IIngredientQuantityScoreBooster.class);
        }

        @Provides
        @Singleton
        ClientRecommender provideClientRecommender() {
            try {
                return new ClientRecommender( new MyrrixClientConfiguration() );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Singleton
    @Component(modules={ TestModule.class })
    public interface TestComponent {
        void inject(final MyrrixUpdaterTest runner);
    }

//        @Provides
//        @Singleton
//        ClientRecommender provideClientRecommender() {
//        	try {
//	            final ClientRecommender mr = mock( ClientRecommender.class );
//
//	            doAnswer( new Answer<Object>() {
//	                public Object answer(InvocationOnMock invocation) {
//	                    Object[] args = invocation.getArguments();
//	                    COUNT++;
//	                    return "called with arguments: " + args;
//	                }
//	            }).when(mr).setItemTag( anyString(), anyLong(), anyFloat());
//
//	            return mr;
//        	}
//        	catch (TasteException e) {
//        		throw Throwables.propagate(e);
//        	}
//        }

    private class RecommenderWrapper {
        private final ClientRecommender finalRecommender;


        public RecommenderWrapper() {
            try {
                finalRecommender = new ClientRecommender( new MyrrixClientConfiguration() );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void setItemTag(String tag, long itemID, float value) {
            try {
                finalRecommender.setItemTag(tag, itemID, value);
            } catch (TasteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
