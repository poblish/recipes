/**
 * 
 */
package uk.co.recipes.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.CommonTags;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class CanonicalItemFactory {

	private final static HttpClient CLIENT = new DefaultHttpClient();

	private final static ObjectMapper MAPPER = new ObjectMapper();

	private final static String	IDX_URL = "http://localhost:9200/recipe/items";


	public static ICanonicalItem put( final ICanonicalItem inItem, String inId) throws IOException {
		final HttpPost req = new HttpPost( IDX_URL + "/" + inId);

		try {
			req.setEntity( new StringEntity( MAPPER.writeValueAsString(inItem) ) );

			final HttpResponse resp = CLIENT.execute(req);
			assertThat( resp.getStatusLine().getStatusCode(), is(201));
			EntityUtils.consume( resp.getEntity() );
		}
		catch (UnsupportedEncodingException e) {
			Throwables.propagate(e);
		}

		return inItem;
	}

	public static ICanonicalItem get( String inId) throws IOException {
		SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
		testModule.addKeyDeserializer( ITag.class, new KeyDeserializer() {

			@Override
			public Object deserializeKey( String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				return CommonTags.valueOf(key);
			}} );

		testModule.addDeserializer( Serializable.class, new JsonDeserializer<Serializable>() {

			@Override
			public Serializable deserialize( JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				JsonToken t = jp.getCurrentToken();
				return t.asString();
			}} );

		MAPPER.registerModule(testModule);

		return MAPPER.readValue( MAPPER.readTree( new URL( IDX_URL + "/" + inId) ).path("_source"), CanonicalItem.class);
	}

	public static ICanonicalItem getOrCreate( final String inCanonicalName, final Supplier<ICanonicalItem> inCreator) throws IOException {
		final String nameId = inCanonicalName.toLowerCase().replace( ' ', '_');

		try {
			final ICanonicalItem item = get(nameId);
			if ( item != null) {
				return item;
			}
		}
		catch (FileNotFoundException e) { /* Not found! */ }

		return put( inCreator.get(), nameId);
	}
}
