/**
 * 
 */
package uk.co.recipes.test;

import static java.util.Locale.ENGLISH;
import static uk.co.recipes.metrics.MetricNames.TIMER_RECIPE_PARSE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import com.codahale.metrics.Timer.Context;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Recipe;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.parse.DeferralStatus;
import uk.co.recipes.parse.IDeferredIngredientHandler;
import uk.co.recipes.parse.IParsedIngredientHandler;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.tags.TagUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class TestDataUtils {

	@Inject IngredientParser parser;
	@Inject MetricRegistry metrics;
	@Inject EsUserFactory userFactory;
	@Inject EsItemFactory itemFactory;
	@Inject EsRecipeFactory recipeFactory;

//	private final static Logger LOG = LoggerFactory.getLogger( TestDataUtils.class );

	@Inject
	public TestDataUtils() {
		// For Dagger
	}

	private final static IParsedIngredientHandler NULL_HANDLER = ingr -> { };
	private final static IDeferredIngredientHandler NULL_DEFER_HANDLER = x -> { };


	public boolean parseIngredient( final String inStr) {
		return parser.parse(inStr, NULL_HANDLER, NULL_DEFER_HANDLER);
	}

	public boolean parseIngredient( final String inStr, final IParsedIngredientHandler inHandler, final IDeferredIngredientHandler inDeferHandler) {
		return parser.parse(inStr, inHandler, inDeferHandler);
	}

	public List<IIngredient> parseIngredientsFrom( final IUser adminUser, final String inFilename) throws IOException {
		return parseIngredientsFrom( adminUser, new File("src/test/resources/ingredients/"), inFilename);
	}

	public List<IIngredient> parseIngredientsFrom( final IUser adminUser, final File inDir, final String inFilename) throws IOException {
		try (Context ctxt = metrics.timer(TIMER_RECIPE_PARSE).time()) {
			return timedParseIngredientsFrom( adminUser, inDir, inFilename);
		}
	}

	private List<IIngredient> timedParseIngredientsFrom( final IUser adminUser, final File inDir, final String inFilename) throws IOException {
		final List<IIngredient> allIngredients = Lists.newArrayList();

		String recipeTitle = null;
		int lineNum = 0;

		final List<DeferralStatus> deferredItems = Lists.newArrayList();

		Map<ITag,Serializable> recipeTags = null;

		for ( String eachLine : Files.readLines( new File( inDir, inFilename), Charset.forName("utf-8"))) {

			if (eachLine.isEmpty()) {
				continue;
			}

			lineNum++;

			// Parse comments
			if (eachLine.startsWith("// ")) {
				if ( lineNum == 1) {
					recipeTitle = eachLine.substring(3).trim();
				}
				continue;
			}

			// Parse Recipe tags
			if (eachLine.startsWith("@")) {
				if ( recipeTags == null) {
					recipeTags = Maps.newHashMap();
				}

				int eqPos = eachLine.indexOf('=');
				String tagName = eachLine.substring( 1, eqPos);

				if (tagName.startsWith("recipe")) {  // Yuk, insert underscore between words
					tagName = "recipe_" + tagName.substring(6);
				}

				recipeTags.put( TagUtils.forName( tagName.toUpperCase() ), eachLine.substring( eqPos + 1));
				continue;
			}

			///////////////////////////////////////////////////////////////////////////////////////////

			boolean matched = parser.parse( eachLine, allIngredients::add, deferredItems::add);

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

		final RecipeStage stage1 = new RecipeStage();
		stage1.addIngredients(allIngredients);

		final Recipe r = new Recipe(adminUser, inFilename, Locale.UK);
		r.addStage(stage1);

		if ( recipeTitle != null) {
			r.setTitle(recipeTitle);
		}

		if ( recipeTags != null) {
			r.addTags(recipeTags);
		}

		recipeFactory.put( r, recipeFactory.toStringId(r));

		////////////////////////////////////////////////////////////

		return allIngredients;
	}
}
