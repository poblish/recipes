/**
 * 
 */
package uk.co.recipes.persistence;

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

import uk.co.recipes.api.ITag;
import uk.co.recipes.tags.TagUtils;

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

		MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // When reading in ES hits, e.g. _index
		MAPPER.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, false);

		MAPPER.registerModule(testModule);

	}

	public static ObjectMapper getMapper() {
		return MAPPER;
	}
}
