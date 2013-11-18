/**
 * 
 */
package uk.co.recipes.myrrix;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.yaml.snakeyaml.Yaml;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MyrrixPrefsIngester {

	@Inject EsItemFactory itemFactory;
	@Inject ClientRecommender myrrix;

	public String parseRecommendations( final File inFile) throws IOException {
		final StringBuilder sb = new StringBuilder();

		visitRecommendationsFile( inFile, new UserPrefsVisitor() {

            @Override
            public void visit( int userId, Map<String, Object> eachUserPref) throws IOException {
                if ( eachUserPref.containsKey("block") ||
                    ( /* All done, don't create '1' rating */ eachUserPref.containsKey("fave") && !eachUserPref.containsKey("score"))) {
                    return;
                }

                ////////////////////////////////  Deal with ratings/scores

                if ( sb.length() > 0) {
                    sb.append('\r');
                }

                final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i") ).get();
                sb.append(userId).append(',').append( item.getId() ).append(',').append( Objects.firstNonNull( eachUserPref.get("score"), "1"));
            }}
		);

		System.out.println("Parsed: " + sb.toString().replace( '\r', ' ') + "  (" + sb.length() + " chars)");

		return sb.toString();
	}

    public String parseFaves( final File inFile) throws IOException {
        visitRecommendationsFile( inFile, new UserPrefsVisitor() {

            @Override
            public void visit( int userId, Map<String, Object> eachUserPref) throws IOException {
                if (eachUserPref.containsKey("block")) {
                    return;
                }

                if (eachUserPref.containsKey("fave")) {
                    final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i") ).get();
                    System.out.println("Fave: " + item);
                }
            }}
        );

        return "ok";
    }

    public String parseBlocks( final File inFile) throws IOException {
        visitRecommendationsFile( inFile, new UserPrefsVisitor() {

            @Override
            public void visit( int userId, Map<String, Object> eachUserPref) throws IOException {
                if (eachUserPref.containsKey("block")) {
                    final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i") ).get();
                    System.out.println("BLOCK: " + item);
                }
            }}
        );

        return "ok";
    }

    private void visitRecommendationsFile( final File inFile, final UserPrefsVisitor inVisitor) throws IOException {
        for ( Object eachDoc : new Yaml().loadAll( Files.toString( inFile, Charset.forName("utf-8")))) {

            @SuppressWarnings("unchecked")
            final Map<Integer,List<Map<String,Object>>> eachDocMap = (Map<Integer,List<Map<String,Object>>>) eachDoc;

            for ( Entry<Integer,List<Map<String,Object>>> eachUserDoc : eachDocMap.entrySet()) {
                final int userId =  eachUserDoc.getKey();

                for ( Map<String,Object> eachUserPref : eachUserDoc.getValue()) {
                    try {
                        inVisitor.visit( userId, eachUserPref);
                    }
                    catch (IOException e) {
                        System.err.println("IGNORE: " + e);
                    }
                }
            }
        }
    }

	public void ingestRecommendations( final String inData) throws IOException {
		try {
			myrrix.ingest( new StringReader(inData) );
			myrrix.refresh();
		}
		catch (TasteException e) {
			Throwables.propagate(e);
		}
	}

	private static interface UserPrefsVisitor {
	    void visit( final int userId, final Map<String,Object> eachUserPref) throws IOException;
	}
}