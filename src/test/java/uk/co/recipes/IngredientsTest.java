package uk.co.recipes;

import java.util.Locale;

import org.testng.annotations.Test;

import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;

import com.google.gson.Gson;

public class IngredientsTest {

	@Test
	public void initialTest() {
		final CanonicalItem ci1 = new CanonicalItem("Lamb");
		final CanonicalItem ci2 = new CanonicalItem("Lamb Neck");
		ci1.addVariety(ci2);

		final IQuantity q = new IQuantity() {

			@Override
			public int number() {
				return 900;
			}

			@Override
			public IUnit units() {
				return Units.GRAMMES;
			}};

		final NamedItem n1 = new NamedItem(ci2);
		final Ingredient i1 = new Ingredient( n1, q);
		i1.addNote( Locale.ENGLISH, "Neck fillets, cut into large chunks");

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredient(i1);

		final Recipe r = new Recipe();
		r.addStage(stage1);
		r.addTag( CommonTags.SERVES_COUNT, "4");

		System.out.println( new Gson().toJson(r) );
	}
}
