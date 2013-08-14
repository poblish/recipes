/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.io.Serializable;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.ForkDetails;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.Recipe;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.User;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IForkDetails;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.Units;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.tags.TagUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public final class JacksonFactory {

    private JacksonFactory() {}

	public static void initialiseMapper( final ObjectMapper inMapper) {
		SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, null, null));
		testModule.addKeyDeserializer( ITag.class, new KeyDeserializer() {

			@Override
			public Object deserializeKey( String key, DeserializationContext ctxt) {
				return TagUtils.forName(key);
			}} );

        testModule.addDeserializer( Serializable.class, new JsonDeserializer<Serializable>() {

            @Override
            public Serializable deserialize( JsonParser jp, DeserializationContext ctxt) {
                JsonToken t = jp.getCurrentToken();
                return t.asString();
            }} );

        testModule.addDeserializer( IUnit.class, new JsonDeserializer<IUnit>() {

            @Override
            public IUnit deserialize( JsonParser jp, DeserializationContext ctxt) {
                try {
                    return Units.valueOf( jp.getText() );
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }} );

        testModule.addAbstractTypeMapping( IRecipe.class, Recipe.class);
        testModule.addAbstractTypeMapping( IRecipeStage.class, RecipeStage.class);
		testModule.addAbstractTypeMapping( IIngredient.class, Ingredient.class);
		testModule.addAbstractTypeMapping( ICanonicalItem.class, CanonicalItem.class);
        testModule.addAbstractTypeMapping( IQuantity.class, Quantity.class);
        testModule.addAbstractTypeMapping( IItemRating.class, ItemRating.class);
        testModule.addAbstractTypeMapping( IRecipeRating.class, RecipeRating.class);
        testModule.addAbstractTypeMapping( IForkDetails.class, ForkDetails.class);
        testModule.addAbstractTypeMapping( IUser.class, User.class);

        inMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // When reading in ES hits, e.g. _index
        inMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        inMapper.registerModule(testModule);
        inMapper.registerModule(new JodaModule());
	}
}
