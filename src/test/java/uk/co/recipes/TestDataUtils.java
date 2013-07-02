/**
 * 
 */
package uk.co.recipes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.testng.Assert;

import uk.co.recipes.api.IIngredient;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.RecipeFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class TestDataUtils {

	public static List<IIngredient> parseIngredientsFrom( final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/" + inFilename), Charset.forName("utf-8"))) {

			if (eachLine.startsWith("// ")) {
				continue;
			}

			final Optional<Ingredient> theIngr = IngredientParser.parse(eachLine);
			if (theIngr.isPresent()) {

				// System.out.println( JacksonFactory.getMapper().writeValueAsString( theIngr.get() ) );

				allIngredients.add( theIngr.get() );
			}
			else {
				Assert.fail(eachLine + " not matched");
			}
		}

		////////////////////////////////////////////////////////////

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredients(allIngredients);

		final Recipe r = new Recipe(inFilename);
		r.addStage(stage1);

		RecipeFactory.put( r, RecipeFactory.toId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}
}