/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import uk.co.recipes.tags.RecipeTags;
import java.util.Locale;
import org.testng.annotations.Test;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.Units;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeTest {

	@Test
	public void testEqualsHashAndClone() {
		final IQuantity q1 = new Quantity( Units.GRAMMES, 100);

		final ICanonicalItem ingr1 = new CanonicalItem("Lamb");
		final ICanonicalItem ingr2 = new CanonicalItem("Beef");

		final User user = new User( "aregan", "Andrew R");

		final RecipeStage rs1 = new RecipeStage();
		rs1.addIngredients( new Ingredient( ingr1, q1), new Ingredient( ingr2, q1) );

		final Recipe r1 = new Recipe(user, "1", Locale.UK);
		r1.addStage(rs1);

		final Recipe r2 = new Recipe(user, "1", Locale.UK);
		r2.addStage(rs1);

		final Recipe r3 = new Recipe(user, "1", Locale.UK);

		final Recipe r4 = new Recipe(user, "4", Locale.UK);
		r4.addStage(rs1);

		final Recipe r5 = new Recipe(user, "1", Locale.UK);
		r5.addStage(rs1);
		r5.addTag( RecipeTags.SERVES_COUNT, 4);

		TestUtils.testEqualsHashcode(r1, r2, r3, r4, r5);

		assertThat( r5.toString(), is("Recipe{title=1, id=NEW, creator=User{id=NEW, username=aregan, displayName=Andrew R}, stages=[RecipeStage{ingredients=[Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Lamb}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Beef}}]}], tags={SERVES_COUNT=4}, locale=en_GB}"));

        assertThat((Recipe) r1.clone(), is(r1));
        assertThat((Recipe) r2.clone(), is(r2));
        assertThat((Recipe) r3.clone(), is(r3));
        assertThat((Recipe) r4.clone(), is(r4));
        assertThat((Recipe) r5.clone(), is(r5));
	}
}
