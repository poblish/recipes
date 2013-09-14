/**
 * 
 */
package uk.co.recipes.test;

import static java.util.Locale.ENGLISH;

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
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IUser;
import uk.co.recipes.parse.DeferralStatus;
import uk.co.recipes.parse.IDeferredIngredientHandler;
import uk.co.recipes.parse.IParsedIngredientHandler;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.EsItemFactory;
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

	@Inject IngredientParser parser;
	@Inject EsUserFactory userFactory;
	@Inject EsItemFactory itemFactory;
	@Inject EsRecipeFactory recipeFactory;

//	private final static Logger LOG = LoggerFactory.getLogger( TestDataUtils.class );

	private final static IParsedIngredientHandler NULL_HANDLER = new IParsedIngredientHandler() {
		@Override
		public void foundIngredient( IIngredient ingr) { }
	};

	private final static IDeferredIngredientHandler NULL_DEFER_HANDLER = new IDeferredIngredientHandler() {
		@Override
		public void deferIngredient( final DeferralStatus x) { }
	};


	public boolean parseIngredient( final String inStr) {
		return parser.parse(inStr, NULL_HANDLER, NULL_DEFER_HANDLER);
	}

	public boolean parseIngredient( final String inStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
		return parser.parse(inStr, inHandler, inDeferHandler);
	}

	public List<IIngredient> parseIngredientsFrom( final String inFilename) throws IOException {
		return parseIngredientsFrom("src/test/resources/ingredients/", inFilename);
	}

	public List<IIngredient> parseIngredientsFrom( final String inDir, final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		String recipeTitle = null;
		int lineNum = 0;

		final List<DeferralStatus> deferredItems = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File( inDir, inFilename), Charset.forName("utf-8"))) {

			if (eachLine.isEmpty()) {
				continue;
			}

			lineNum++;

			if (eachLine.startsWith("// ")) {
				if ( lineNum == 1) {
					recipeTitle = eachLine.substring(3).trim();
				}
				continue;
			}

			boolean matched = parser.parse( eachLine, new IParsedIngredientHandler() {

				@Override
				public void foundIngredient( final IIngredient ingr) {
					allIngredients.add(ingr);
				}
			}, new IDeferredIngredientHandler() {

				@Override
				public void deferIngredient( DeferralStatus status) {
					deferredItems.add(status);
				}
			} );

			if (!matched) {
				throw new RuntimeException(eachLine + " not matched");
			}
		}

		////////////////////////////////////////////////////////////  Handle deferred...

		if (!deferredItems.isEmpty()) {
			for ( DeferralStatus eachItem : deferredItems) {

				final Optional<ICanonicalItem> matchedItem = itemFactory.findBestMatchByName( eachItem.getNamePossibilities() );

		    	final ICanonicalItem gotOrCreatedItem = matchedItem.isPresent() ? matchedItem.get() : parser.findItem( eachItem.getOriginalName() );

				final Ingredient ingr = new Ingredient( gotOrCreatedItem, eachItem.getQuantity(), Boolean.TRUE);

				if ( eachItem.getNote() != null) {
					ingr.addNote( ENGLISH, eachItem.getNote().startsWith(",") ? eachItem.getNote().substring(1).trim() : eachItem.getNote());
				}

				ingr.addNotes( ENGLISH, eachItem.getExtraNotes());

				allIngredients.add(ingr);
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

		if ( recipeTitle != null) {
			r.setTitle(recipeTitle);
		}

		recipeFactory.put( r, recipeFactory.toStringId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}
}
