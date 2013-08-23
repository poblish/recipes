/**
 * 
 */
package uk.co.recipes.events.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import java.io.Serializable;
import java.util.Map;
import javax.inject.Singleton;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.tags.CommonTags;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


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

    @Test
    public void testAddItem() {
        final Map<ITag,Serializable> tags = Maps.newHashMap();
        tags.put( CommonTags.SPICE, Boolean.TRUE);
        tags.put( CommonTags.INDIAN, "3.0");  // Try boosting

        final ICanonicalItem item = mock( ICanonicalItem.class );
        when( item.getCanonicalName() ).thenReturn("ginger");
        when( item.getTags() ).thenReturn(tags);
        when( item.parent() ).thenReturn(ABSENT);

        assertThat( COUNT, is(0));

        MU.onAddItem( new AddItemEvent(item) );

        assertThat( COUNT, is(3)); // Parent tag + two Tags
    }

    @Module( includes=DaggerModule.class, injects={}, overrides=true)
    static class TestModule {

        @Provides
        @Singleton
        ClientRecommender provideClientRecommender() throws TasteException {
            final ClientRecommender mr = mock( ClientRecommender.class );

            doAnswer( new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) {
                    Object[] args = invocation.getArguments();
                    COUNT++;
                    return "called with arguments: " + args;
                }
            }).when(mr).setItemTag( anyString(), anyLong(), anyFloat());

            return mr;
        }
    }
}
