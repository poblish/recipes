/**
 *
 */
package uk.co.recipes.persistence;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Throwables;
import uk.co.recipes.*;
import uk.co.recipes.api.*;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.RecipeRating;
import uk.co.recipes.tags.TagUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * TODO
 *
 * @author andrewregan
 */
public final class JacksonFactory {

    private JacksonFactory() {
    }

    public static void initialiseMapper(final ObjectMapper inMapper) {
        SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, null, null));
        testModule.addKeyDeserializer(ITag.class, new KeyDeserializer() {

            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return TagUtils.forName(key);
            }
        });

        testModule.addDeserializer(Serializable.class, new JsonDeserializer<Serializable>() {

            @Override
            public Serializable deserialize(JsonParser jp, DeserializationContext ctxt) {
                JsonToken t = jp.getCurrentToken();
                if (t.asString() != null) {  // This'll work for JsonToken.VALUE_TRUE and VALUE_FALSE
                    return t.asString();
                }

                try {
                    return jp.getText();  // This'll work for JsonToken.VALUE_STRING
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        testModule.addDeserializer(IUnit.class, new JsonDeserializer<IUnit>() {

            @Override
            public IUnit deserialize(JsonParser jp, DeserializationContext ctxt) {
                try {
                    return Units.valueOf(jp.getText());
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        testModule.addDeserializer(IExplorerFilterItem.class, UserPreferences.explorerFilterItemsDeser());

        testModule.addAbstractTypeMapping(IRecipe.class, Recipe.class);
        testModule.addAbstractTypeMapping(IRecipeStage.class, RecipeStage.class);
        testModule.addAbstractTypeMapping(IIngredient.class, Ingredient.class);
        testModule.addAbstractTypeMapping(ICanonicalItem.class, CanonicalItem.class);
        testModule.addAbstractTypeMapping(IQuantity.class, Quantity.class);
        testModule.addAbstractTypeMapping(IItemRating.class, ItemRating.class);
        testModule.addAbstractTypeMapping(IRecipeRating.class, RecipeRating.class);
        testModule.addAbstractTypeMapping(IForkDetails.class, ForkDetails.class);
        testModule.addAbstractTypeMapping(IUser.class, User.class);
        testModule.addAbstractTypeMapping(IUserAuth.class, UserAuth.class);
        testModule.addAbstractTypeMapping(IUserPreferences.class, UserPreferences.class);

        testModule.addDeserializer(ITag.class, new JsonDeserializer<ITag>() {

            @Override
            public ITag deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                try {
                    return TagUtils.forName(jp.getText());
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        inMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // When reading in ES hits, e.g. _index
        inMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        inMapper.registerModule(testModule)
                // https://github.com/FasterXML/jackson-modules-java8
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                /* Not needed: .registerModule(new ParameterNamesModule()) */;
    }
}
