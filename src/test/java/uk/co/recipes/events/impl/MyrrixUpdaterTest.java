/**
 * 
 */
package uk.co.recipes.events.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import javax.inject.Singleton;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.Recipe;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.User;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.Units;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.NationalCuisineTags;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class MyrrixUpdaterTest {

    private final static ObjectGraph GRAPH = ObjectGraph.create( new TestModule() );

    private final static MyrrixUpdater MU = GRAPH.get( MyrrixUpdater.class );

    private final static Optional<ICanonicalItem> ABSENT = Optional.absent();

    private static int COUNT = 0;

    @Test(enabled = false)
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

        MU.onAddItem( new AddItemEvent(item) );

        assertThat( COUNT, is(3)); // Parent tag + two Tags
    }

    @Test(enabled = false)
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

        MU.onAddRecipe( new AddRecipeEvent(r1) );

        assertThat( COUNT, is(6)); // Parent tag + two Tags
    }

    @Module( includes=DaggerModule.class, overrides=true)
    static class TestModule {

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
    }
}
