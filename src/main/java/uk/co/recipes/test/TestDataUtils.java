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
import uk.co.recipes.User;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IUser;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
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
	EsUserFactory userFactory;

	@Inject
	EsRecipeFactory recipeFactory;

	public Optional<Ingredient> parseIngredient( final String inStr) {
		return parser.parse(inStr);
	}

	public List<IIngredient> parseIngredientsFrom( final String inFilename) throws IOException {
		return parseIngredientsFrom("src/test/resources/ingredients/", inFilename);
	}

	public List<IIngredient> parseIngredientsFrom( final String inDir, final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File( inDir, inFilename), Charset.forName("utf-8"))) {

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

		final IUser adminUser = userFactory.getOrCreate( "Admin", new Supplier<IUser>() {

			@Override
			public IUser get() {
				return new User( "admin", "Admin");
			}
		} );

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredients(allIngredients);

		final Recipe r = new Recipe(adminUser, inFilename, Locale.UK);
		r.addStage(stage1);

		recipeFactory.put( r, recipeFactory.toStringId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}
}
