package uk.co.recipes.events.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.co.recipes.*;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.Units;
import uk.co.recipes.mocks.MockFactories;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.NationalCuisineTags;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static uk.co.recipes.api.Units.GRAMMES;


public class MyrrixUpdaterTest {

    @Inject
    MyrrixUpdater updater;

    private static final Optional<ICanonicalItem> ABSENT = Optional.absent();

    private static int COUNT;

    @BeforeClass
    private void injectDependencies() {
        DaggerMyrrixUpdaterTest_TestComponent.builder()
                .testItemsModule( new TestItemsModule() )
                .testMyrrixModule( new TestMyrrixModule() )
                .build()
                .inject(this);
    }

    @BeforeMethod
    private void resetCount() {
        COUNT = 0;
    }

    @Test
    public void testAddItem() {
        final Map<ITag,Serializable> tags = Maps.newHashMap();
        tags.put(CommonTags.SPICE, Boolean.TRUE);
        tags.put(NationalCuisineTags.INDIAN, "3.0");  // Try boosting

        final ICanonicalItem item = mock(ICanonicalItem.class);
        when(item.getCanonicalName()).thenReturn("ginger");
        when(item.getTags()).thenReturn(tags);
        when(item.parent()).thenReturn(ABSENT);

        /////////////////////////////////////////////////

        assertThat(COUNT, is(0));

        updater.onAddItem(new AddItemEvent(item));

        // 1/8/2017: Was this ever working? assertThat( COUNT, is(3)); // Parent tag + two Tags
        assertThat(COUNT, is(2)); // Just two Tags
    }

    @Test
    public void testAddRecipe() throws IOException {
        final Map<ITag,Serializable> tags = Maps.newHashMap();
        tags.put(CommonTags.SPICE, Boolean.TRUE);
        tags.put(NationalCuisineTags.INDIAN, "3.0");  // Try boosting

        final ICanonicalItem item = mock(ICanonicalItem.class);
        when(item.getCanonicalName()).thenReturn("ginger");
        when(item.getTags()).thenReturn(tags);
        when(item.parent()).thenReturn(ABSENT);
        when(item.getBaseAmount()).thenReturn(Optional.of(new Quantity(GRAMMES, 100)));

        final User user = new User("aregan", "Andrew R");

        final RecipeStage rs1 = new RecipeStage();
        rs1.addIngredients(new Ingredient(item, new Quantity(Units.TSP, 1)));

        final Recipe r1 = new Recipe(user, "1", Locale.UK);
        r1.addStage(rs1);

        r1.setId(Recipe.BASE_ID);

        /////////////////////////////////////////////////

        assertThat(COUNT, is(0));

        updater.onAddRecipe(new AddRecipeEvent(r1));

        // 1/8/2017: Was this ever working? assertThat( COUNT, is(3)); // Parent tag + two Tags
        assertThat(COUNT, is(2)); // Just two Tags
    }

    @Module
    public class TestMyrrixModule {
        @Provides
        @Singleton
        ClientRecommender provideClientRecommender() {
            try {
                final ClientRecommender mr = mock(ClientRecommender.class);

                doAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    System.out.println(Arrays.toString(args));
                    COUNT++;
                    return "called with arguments: " + Arrays.toString(args);
                }).when(mr).setItemTag(anyString(), anyLong(), anyFloat());

                return mr;
            } catch (TasteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Module
    public class TestItemsModule {
        @Provides
        @Singleton
        EsItemFactory provideItemFactory() {
            return MockFactories.inMemoryItemFactory();
        }
    }

    @Singleton
    @Component(modules = {DaggerModule.class, TestMyrrixModule.class, TestItemsModule.class})
    public interface TestComponent {
        void inject(final MyrrixUpdaterTest runner);
    }
}
