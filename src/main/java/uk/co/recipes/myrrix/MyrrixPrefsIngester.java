package uk.co.recipes.myrrix;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.yaml.snakeyaml.Yaml;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MyrrixPrefsIngester {

    @Inject EsItemFactory itemFactory;
    @Inject ClientRecommender myrrix;
    @Inject Yaml yaml;

    @Inject
    public MyrrixPrefsIngester() {
        // For Dagger
    }

    public String parseRecommendations(final File inFile) throws IOException {
        final StringBuilder sb = new StringBuilder();

        visitRecommendationsFile(inFile, (userId, eachUserPref) -> {
                    if (eachUserPref.containsKey("block") ||
                            ( /* All done, don't create '1' rating */ eachUserPref.containsKey("fave") && !eachUserPref.containsKey("score"))) {
                        return;
                    }

                    ////////////////////////////////  Deal with ratings/scores

                    if (sb.length() > 0) {
                        sb.append('\r');
                    }

                    final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i")).get();
                    sb.append(userId).append(',').append(item.getId()).append(',').append(MoreObjects.firstNonNull(eachUserPref.get("score"), "1"));
                }
        );

        System.out.println("Parsed: " + sb.toString().replace('\r', ' ') + "  (" + sb.length() + " chars)");

        return sb.toString();
    }

    public Collection<ICanonicalItem> parseFaves(final File inFile) throws IOException {
        final Set<ICanonicalItem> faves = Sets.newLinkedHashSet();

        visitRecommendationsFile(inFile, (userId, eachUserPref) -> {
                if (eachUserPref.containsKey("block")) {
                    return;
                }

                if (eachUserPref.containsKey("fave")) {
                    final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i")).get();
                    faves.add(item);
                }
            }
        );

        return faves;
    }

    public String parseBlocks(final File inFile) throws IOException {
        visitRecommendationsFile(inFile, (userId, eachUserPref) -> {
                    if (eachUserPref.containsKey("block")) {
                        final ICanonicalItem item = itemFactory.get((String) eachUserPref.get("i")).get();
                        System.out.println("BLOCK: " + item);
                    }
                }
        );

        return "ok";
    }

    private void visitRecommendationsFile(final File inFile, final UserPrefsVisitor inVisitor) throws IOException {
        for (Object eachDoc : yaml.loadAll(Files.toString(inFile, Charset.forName("utf-8")))) {

            @SuppressWarnings("unchecked") final Map<Integer,List<Map<String,Object>>> eachDocMap = (Map<Integer,List<Map<String,Object>>>) eachDoc;

            for (Entry<Integer,List<Map<String,Object>>> eachUserDoc : eachDocMap.entrySet()) {
                final int userId = eachUserDoc.getKey();

                for (Map<String,Object> eachUserPref : eachUserDoc.getValue()) {
                    try {
                        inVisitor.visit(userId, eachUserPref);
                    } catch (IOException e) {
                        System.err.println("IGNORE: " + e);
                    }
                }
            }
        }
    }

    public void ingestRecommendations(final String inData) {
        try {
            myrrix.ingest(new StringReader(inData));
            myrrix.refresh();
        } catch (TasteException e) {
            throw new RuntimeException(e);
        }
    }

    private interface UserPrefsVisitor {
        void visit(final int userId, final Map<String,Object> eachUserPref) throws IOException;
    }
}