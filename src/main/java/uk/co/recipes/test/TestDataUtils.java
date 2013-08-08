/**
 * 
 */
package uk.co.recipes.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import uk.co.recipes.Ingredient;
import uk.co.recipes.Recipe;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.EsRecipeFactory;

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

	@Inject
	IngredientParser parser;

	@Inject
	EsRecipeFactory recipeFactory;

	public Optional<Ingredient> parseIngredient( final String inStr) {
		return parser.parse(inStr);
	}

	public List<IIngredient> parseIngredientsFrom( final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/" + inFilename), Charset.forName("utf-8"))) {

			if (eachLine.isEmpty() || eachLine.startsWith("// ")) {
				continue;
			}

			final Optional<Ingredient> theIngr = parser.parse(eachLine);
			if (theIngr.isPresent()) {

				// System.out.println( JacksonFactory.getMapper().writeValueAsString( theIngr.get() ) );

				allIngredients.add( theIngr.get() );
			}
			else {
				throw new RuntimeException(eachLine + " not matched");
			}
		}

		////////////////////////////////////////////////////////////

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredients(allIngredients);

		final Recipe r = new Recipe(inFilename, Locale.UK);
		r.addStage(stage1);

		recipeFactory.put( r, recipeFactory.toStringId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}
}
