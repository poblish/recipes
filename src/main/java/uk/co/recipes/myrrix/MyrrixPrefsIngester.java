/**
 * 
 */
package uk.co.recipes.myrrix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.yaml.snakeyaml.Yaml;

import uk.co.recipes.persistence.EsItemFactory;

import com.google.common.base.Objects;
import com.google.common.io.Files;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixPrefsIngester {

	@Inject EsItemFactory itemFactory;

	public String parseRecommendations( final String inPath) throws IOException {
		final StringBuilder sb = new StringBuilder();

		for ( Object eachDoc : new Yaml().loadAll( Files.toString( new File(inPath), Charset.forName("utf-8")))) {

			@SuppressWarnings("unchecked")
			final Map<Integer,List<Map<String,Object>>> eachDocMap = (Map<Integer,List<Map<String,Object>>>) eachDoc;

			for ( Entry<Integer,List<Map<String,Object>>> eachUserDoc : eachDocMap.entrySet()) {
				final int userId =  eachUserDoc.getKey();

				for ( Map<String,Object> eachUserPref : eachUserDoc.getValue()) {
					if ( sb.length() > 0) {
						sb.append('\r');
					}

					try {
						sb.append(userId).append(',').append( itemFactory.get((String) eachUserPref.get("i") ).get().getId() ).append(',').append( Objects.firstNonNull( eachUserPref.get("score"), "1"));
					}
					catch (IOException e) {
						System.err.println("IGNORE: " + e);
					}
				}
			}
		}

		return sb.toString();
	}
}
