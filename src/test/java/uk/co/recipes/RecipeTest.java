/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Locale;

import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.Units;
import uk.co.recipes.tags.CommonTags;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class RecipeTest {

	@Test
	public void testEqualsHash() {
		final IQuantity q1 = new Quantity( Units.GRAMMES, 100);

		final ICanonicalItem ingr1 = new CanonicalItem("Lamb");
		final ICanonicalItem ingr2 = new CanonicalItem("Beef");

		final RecipeStage rs1 = new RecipeStage();
		rs1.addIngredients( new Ingredient( ingr1, q1), new Ingredient( ingr2, q1) );

		final Recipe r1 = new Recipe("1", Locale.UK);
		r1.addStage(rs1);

		final Recipe r2 = new Recipe("1", Locale.UK);
		r2.addStage(rs1);

		final Recipe r3 = new Recipe("1", Locale.UK);

		final Recipe r4 = new Recipe("4", Locale.UK);
		r4.addStage(rs1);

		final Recipe r5 = new Recipe("1", Locale.UK);
		r5.addStage(rs1);
		r5.addTag( CommonTags.SERVES_COUNT, 4);

		TestUtils.testEqualsHashcode(r1, r2, r3, r4, r5);

		assertThat( r5.toString(), is("Recipe{title=1, id=NEW, stages=[RecipeStage{ingredients=[Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Lamb}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Beef}}]}], tags={SERVES_COUNT=4}, locale=en_GB}"));
	}
}
