package uk.co.recipes.myrrix;

import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Reader;

public class MyrrixUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(MyrrixUpdater.class);

    @Inject ClientRecommender recommender;

    @Inject
    public MyrrixUpdater() {
        // For Dagger
    }

    public void refresh() {
        this.recommender.refresh();
    }

    public void ingest(final Reader reader) throws TasteException {
        this.recommender.ingest(reader);
        refresh();
    }

    public boolean setItemTag(String inString, long inItemOrRecipeId, float inScoreToUse) throws TasteException {
        this.recommender.setItemTag(inString, inItemOrRecipeId, inScoreToUse);
        return true;
    }
}