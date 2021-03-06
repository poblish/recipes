/**
 *
 */
package uk.co.recipes;

import dagger.Component;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.*;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.MeatAndFishTags;
import uk.co.recipes.test.TestDataUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;


/**
 * TODO
 *
 * @author andrewr
 */
public class UserPersistenceTest {

    private static final String TEST_RECIPE = "inputs3.txt";

    @Inject
    EsItemFactory itemFactory;
    @Inject
    EsUserFactory userFactory;
    @Inject
    EsRecipeFactory recipeFactory;
    @Inject
    EsSequenceFactory sequenceFactory;
    @Inject
    TestDataUtils dataUtils;
    @Inject
    UserRatings userRatings;
    @Inject
    ItemsLoader loader;

    private void injectDependencies() {
        DaggerUserPersistenceTest_TestComponent.create().inject(this);
    }

    @BeforeClass
    public void cleanIndices() throws IOException {
        injectDependencies();

        userFactory.deleteAll();
        sequenceFactory.deleteAll();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws IOException, InterruptedException {
        loader.load();

        final IUser adminUser = userFactory.adminUser();

        dataUtils.parseIngredientsFrom(adminUser, TEST_RECIPE);

        while (recipeFactory.countAll() < 1) {
            Thread.sleep(200); // Wait for saves to appear...
        }
    }

    @Test
    public void testPersistenceWithPrefs() throws IOException, InterruptedException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final ICanonicalItem item1 = itemFactory.get("Coriander").get();
        final ICanonicalItem item2 = itemFactory.get("Ginger").get();

        final IUser u1 = new User(testUName, testDName);
        u1.getPrefs().explorerIncludeAdd(CommonTags.VEGETABLE);
        u1.getPrefs().explorerExcludeAdd(item1);
        u1.getPrefs().explorerExcludeAdd(item1);  // Try DUPE
        u1.getPrefs().explorerExcludeAdd(MeatAndFishTags.MEAT);
        u1.getPrefs().explorerExcludeAdd(MeatAndFishTags.FISH);
        u1.getPrefs().explorerIncludeAdd(MeatAndFishTags.FISH);  // Reverse the above!
        u1.getPrefs().explorerIncludeAdd(MeatAndFishTags.SAUSAGE);
        u1.getPrefs().explorerExcludeAdd(MeatAndFishTags.SAUSAGE);  // Reverse the above!
        u1.getPrefs().explorerIncludeAdd(item2);
        u1.getPrefs().explorerIncludeAdd(item2);  // Try DUPE
        userFactory.put(u1, userFactory.toStringId(u1));

        userFactory.waitUntilRefreshed();

        final IUser retrievedUser = userFactory.getByName(testUName);
//        assertThat( retrievedUser.getPrefs().getExplorerIncludes(), hasItem( CommonTags.VEGETABLE ));
//        assertThat( retrievedUser.getPrefs().getExplorerExcludes(), hasItem( MeatAndFishTags.MEAT ));
        assertThat(retrievedUser.getPrefs().getExplorerIncludes().toString(), is("[TagFilterItem{tag=VEGETABLE}, TagFilterItem{tag=FISH}, ItemFilterItem{name=Ginger}]"));
        assertThat(retrievedUser.getPrefs().getExplorerExcludes().toString(), is("[ItemFilterItem{name=Coriander}, TagFilterItem{tag=MEAT}, TagFilterItem{tag=SAUSAGE}]"));

        // Why not test this method too?
        assertThat(userFactory.getByName(u1.getUserName()), is(u1));
    }

    @Test
    public void testPersistenceWithAuth() throws IOException, InterruptedException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User(testUName, testDName);
        u1.addAuth(new UserAuth("google", "12345"));
        u1.addAuth(new UserAuth("google", "56789"));
        u1.removeAuth(new UserAuth("google", "12345"));
        assertThat(u1.getAuths().size(), is(1));
        assertThat(u1.getAuths().iterator().next().getAuthId(), is("56789"));
        userFactory.put(u1, userFactory.toStringId(u1));

        userFactory.waitUntilRefreshed();

        assertThat(userFactory.findWithAuth(u1.getAuths().iterator().next()).isPresent(), is(true));

        final IUser retrievedUser = userFactory.getByName(testUName);
        assertThat(retrievedUser.isActive(), is(true));
        assertThat(newHashSet(u1.getAuths()), is(newHashSet(retrievedUser.getAuths())));
    }

    @Test
    public void testPersistenceWithFaves() throws IOException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final ICanonicalItem ginger = itemFactory.get("ginger").get();

        final IUser u1 = new User(testUName, testDName);
        userFactory.put(u1, userFactory.toStringId(u1));
        u1.addFave(ginger);
        userFactory.update(u1);
        assertThat(u1.getFaveRecipes().size(), is(0));
        assertThat(u1.getFaveItems().size(), is(1));
        assertThat(u1.getFaveItems(), hasItem(ginger));

        u1.removeFave(ginger);
        userFactory.update(u1);
        assertThat(u1.getFaveItems().size(), is(0));

        u1.addFave(recipeFactory.get(TEST_RECIPE).get());
        userFactory.update(u1);
        assertThat(u1.getFaveItems().size(), is(0));
        assertThat(u1.getFaveRecipes().size(), is(1));
        assertThat(u1.getFaveRecipes(), hasItem(recipeFactory.get(TEST_RECIPE).get()));
    }

    @Test
    public void testPersistenceWithRatings() throws IOException {
        final String testUName = "aregan_" + System.nanoTime();
        final String testDName = "Andrew Regan #" + System.nanoTime();

        final IUser u1 = new User(testUName, testDName);
        userFactory.put(u1, userFactory.toStringId(u1));
        userRatings.addRating(u1, new ItemRating(itemFactory.get("ginger").get(), 5));
    }

    @Singleton
    @Component(modules = {DaggerModule.class})
    public interface TestComponent {
        void inject(final UserPersistenceTest runner);
    }
}