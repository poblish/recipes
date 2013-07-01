/**
 * 
 */
package uk.co.recipes.persistence;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Ingredient;
import uk.co.recipes.Quantity;
import uk.co.recipes.RecipeStage;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IRecipeStage;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUnit;
import uk.co.recipes.api.Units;
import uk.co.recipes.tags.TagUtils;

import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class JacksonFactory {

	private final static ObjectMapper MAPPER = new ObjectMapper();

	static {
		SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
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

		testModule.addAbstractTypeMapping( IRecipeStage.class, RecipeStage.class);
		testModule.addAbstractTypeMapping( IIngredient.class, Ingredient.class);
		testModule.addAbstractTypeMapping( ICanonicalItem.class, CanonicalItem.class);
        testModule.addAbstractTypeMapping( IQuantity.class, Quantity.class);

		MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // When reading in ES hits, e.g. _index
		MAPPER.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, false);

		MAPPER.registerModule(testModule);

	}

	public static ObjectMapper getMapper() {
		return MAPPER;
	}
}
