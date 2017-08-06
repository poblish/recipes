package uk.co.recipes.service.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import uk.co.recipes.CanonicalItem;
import uk.co.recipes.Recipe;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;
import uk.co.recipes.persistence.EsUtils;
import uk.co.recipes.service.api.ESearchArea;
import uk.co.recipes.service.api.ISearchAPI;
import uk.co.recipes.service.api.ISearchResult;
import uk.co.recipes.tags.TagUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static uk.co.recipes.metrics.MetricNames.*;
import static uk.co.recipes.persistence.EsUtils.queryString;

/**
 * TODO
 *
 * @author andrewregan
 */
public class EsSearchService implements ISearchAPI {

    @Inject
    Client esClient;
    @Inject
    EsUtils esUtils;
    @Inject
    ObjectMapper mapper;
    @Inject
    MetricRegistry metrics;

    @Named("elasticSearchItemsUrl")
    @Inject
    String itemIndexUrl;

    @Inject
    public EsSearchService() {
        // For Dagger
    }

    @Override
    public List<ICanonicalItem> findItemsByName(String inName) throws IOException {
        return findItemsByName(inName, false);
    }

    private List<ICanonicalItem> findItemsByName(String inName, boolean inSortByName) throws IOException {
        try (Context ignored = metrics.timer(TIMER_ITEMS_SEARCHES).time()) {
/*			FIXME No, I don't know why this doesn't work...

			if (inSortByName) {
				final SearchHit[] sortedHits = esClient.prepareSearch("recipe").setTypes("items").setSize(9999).setQuery( multiMatchQuery( "tags", inName) ).addSort( "canonicalName", SortOrder.ASC).setTrackScores(false).execute().get().getHits().hits();

				for ( SearchHit each : sortedHits) {
					results.add( mapper.readValue( each.getSourceAsString(), CanonicalItem.class) );
				}
			} */

            final JsonNode jn = mapper.readTree(new URL(itemIndexUrl + "/_search?q=" + URLEncoder.encode(inName, "utf-8") + "&size=9999")).path("hits").path("hits");

            final List<ICanonicalItem> results = Lists.newArrayList();

            for (final JsonNode each : jn) {
                results.add(mapper.readValue(each.path("_source").traverse(), CanonicalItem.class));  // FIXME Remove _source stuff where possible
            }

            // Yuk FIXME by letting ES do this right!
            if (inSortByName) {
                results.sort((o1, o2) -> o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName()));
            }

            return results;
        } catch (MalformedURLException | JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.ISearchAPI#findRecipesByName(java.lang.String)
     */
    @Override
    public List<IRecipe> findRecipesByName(String inName) throws IOException {
        return findRecipesByName(inName, 9999);
    }

    @Override
    public List<IRecipe> findRecipesByName(final String inName, final int inSize) throws IOException {
        final SearchResponse resp;

        try (Context ignored = metrics.timer(TIMER_RECIPES_SEARCHES_SEARCH).time()) {
            resp = esClient.prepareSearch("recipe").setTypes("recipes").setSize(inSize)
                    .setQuery(queryString(inName)
// FIXME 5/8/17               .field("recipes.stages.ingredients.item.canonicalName")
// FIXME 5/8/17               .field("recipes.stages.ingredients.item.constituents.canonicalName", 0.5f)
// FIXME 5/8/17               .field("title", 1.1f)
                    )
                    .execute().actionGet();
        }

        try (Context deserTimer = metrics.timer(TIMER_RECIPES_SEARCHES_DESER).time()) {
            return esUtils.deserializeRecipeHits(resp.getHits().getHits());
        }
    }

    @Override
    public List<ICanonicalItem> findItemsByTag(final ITag inTag) throws IOException {
        return findItemsByTag(inTag, "true");
    }

    // FIXME Shameless copy/paste job
    @Override
    public List<ICanonicalItem> findItemsByTag(final ITag inTag, final String inValue) throws IOException {
        try (Context ignored = metrics.timer(TIMER_ITEMS_SEARCHES).time()) {
            final SearchHit[] hits = esClient.prepareSearch("recipe").setTypes("items").setQuery(queryString(/* "tags." */ "tags." + inTag + ":" + inValue)).setSize(9999).execute().get().getHits().getHits();

            final List<ICanonicalItem> results = Lists.newArrayList();

            for (final SearchHit eachHit : hits) {
                results.add(mapper.readValue(EsUtils.toBytes(eachHit.getSourceRef()), CanonicalItem.class));
            }

            results.sort((o1, o2) -> o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName()));

            return results;
        } catch (InterruptedException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public long[] findRecipeIdsByTag(final ITag inTag) {
        return findRecipeIdsByTag(inTag, "true");
    }

    // FIXME Shameless copy/paste job
    @Override
    public long[] findRecipeIdsByTag(final ITag inTag, final String inValue) {
        try (Context ignored = metrics.timer(TIMER_RECIPES_IDS_SEARCHES).time()) {
            final SearchHit[] hits = esClient.prepareSearch("recipe").setTypes("recipes").setQuery(queryString(/* "tags." */ "\\*." + inTag + ":" + inValue)).setSize(9999).execute().get().getHits().getHits();

            final long[] ids = new long[hits.length];
            int i = 0;

            for (SearchHit each : hits) {
                ids[i++] = Long.parseLong(each.getId());
            }

            return ids;
        } catch (InterruptedException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.ISearchAPI#countItemsByName(java.lang.String)
	 */
    @Override
    public int countItemsByName(String inName) {
        return (int) esClient.prepareSearch("recipe").setTypes("items").setQuery(queryString(inName)).setSize(0).execute().actionGet().getHits().getTotalHits();
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.service.api.ISearchAPI#countRecipesByName(java.lang.String)
     */
    @Override
    public int countRecipesByName(String inName) {
        return (int) esClient.prepareSearch("recipe").setTypes("recipes").setQuery(queryString(inName)).setSize(0).execute().actionGet().getHits().getTotalHits();
    }

    @Override
    public int countItemsByTag(final ITag inTag) {
        return countItemsByName(tagString(inTag));
    }

    @Override
    public int countRecipesByTag(final ITag inTag) {
        return countRecipesByName(tagString(inTag));
    }

    private String tagString(final ITag inTag) {
        return inTag + ":true";
    }

    @Override
    public List<ITag> findTagsByName(String inName) {
        // A bit of a cheat...
        return TagUtils.findTagsByName(inName);
    }

    @Override
    public int countTagsByName(String inName) {
        return findTagsByName(inName).size();
    }

    @Override
    public List<IRecipe> findRandomRecipesByTag(int inCount, final ITag inTag) throws IOException {
        final List<IRecipe> recipesToChooseFrom = findRecipesByName(tagString(inTag), inCount * 3);
        Collections.shuffle(recipesToChooseFrom);
        return recipesToChooseFrom.subList(0, Math.min(inCount, recipesToChooseFrom.size()));
    }

    @Override
    public List<IRecipe> findRecipesByItemName(final ICanonicalItem... inItems) throws IOException {
        Set<String> cNames = Sets.newHashSet();  // FIXME Factor this out somewhere

        for (ICanonicalItem each : inItems) {
            cNames.add(each.getCanonicalName());
        }

        return findRecipesByItemName(Iterables.toArray(cNames, String.class));
    }

    @Override
    public long[] findRecipeIdsByItemName(final ICanonicalItem... inItems) {
        Set<String> cNames = Sets.newHashSet();  // FIXME Factor this out somewhere

        for (ICanonicalItem each : inItems) {
            cNames.add(each.getCanonicalName());
        }

        return findRecipeIdsByItemName(Iterables.toArray(cNames, String.class));
    }

    // FIXME - a very crude implementation
    @Override
    public List<IRecipe> findRandomRecipesByItemName(int inCount, final ICanonicalItem... inItems) throws IOException {
        Set<String> cNames = Sets.newHashSet();  // FIXME Factor this out somewhere

        for (ICanonicalItem each : inItems) {
            cNames.add(each.getCanonicalName());
        }

        final List<IRecipe> recipesToChooseFrom = findNRecipesByItemName(inCount * 3, Iterables.toArray(cNames, String.class));
        Collections.shuffle(recipesToChooseFrom);
        return recipesToChooseFrom.subList(0, Math.min(inCount, recipesToChooseFrom.size()));
    }

    @Override
    public List<IRecipe> findRecipesByItemName(String... inNames) throws IOException {
        return findNRecipesByItemName(9999, inNames);
    }

    @Override
    public long[] findRecipeIdsByItemName(String... inNames) {
        return findNRecipeIdsByItemName(9999, inNames);
    }

    private BoolQueryBuilder getBooleanQueryForNames(final String... inNames) {
        final BoolQueryBuilder booleanQ = boolQuery();
        for (String eachName : inNames) {
            // So, when including a parent like 'Pasta', we should pick up all children too.
            final BoolQueryBuilder boolParentQ = boolQuery();
            boolParentQ.should(matchPhraseQuery("canonicalName", eachName));
            boolParentQ.should(matchPhraseQuery("stages.ingredients.item.parent.canonicalName", eachName));
            booleanQ.must(boolParentQ);
        }
        return booleanQ;
    }

    private List<IRecipe> findNRecipesByItemName(final int inCount, String... inNames) throws IOException {
        if (inNames.length == 0) {
            return Collections.emptyList();
        }

        final BoolQueryBuilder booleanQ = getBooleanQueryForNames(inNames);

        final SearchResponse resp = esClient.prepareSearch("recipe").setTypes("recipes").setQuery(booleanQ).setSize(inCount).execute().actionGet();

        return esUtils.deserializeRecipeHits(resp.getHits().getHits());
    }

    private long[] findNRecipeIdsByItemName(final int inCount, String... inNames) {
        if (inNames.length == 0) {
            return new long[0];
        }

        final BoolQueryBuilder booleanQ = getBooleanQueryForNames(inNames);

        final SearchHit[] hits = esClient.prepareSearch("recipe").setTypes("recipes").setQuery(booleanQ).setFetchSource(false).setSize(inCount).execute().actionGet().getHits().getHits();

        if (hits.length == 0) {
            return new long[0];
        }

        final long[] ids = new long[hits.length];
        int i = 0;

        for (SearchHit each : hits) {
            ids[i++] = Long.parseLong(each.getId());
        }

        return ids;
    }

    @Override
    public int countRecipesByItemName(String... inNames) {
        if (inNames.length == 0) {
            return 0;
        }

        final Context timerCtxt = metrics.timer(TIMER_ITEMS_COUNT_AMONG_RECIPES).time();

        try {
            final BoolQueryBuilder booleanQ = getBooleanQueryForNames(inNames);

            return (int) esClient.prepareSearch("recipe").setTypes("recipes").setQuery(booleanQ).setSize(0).execute().actionGet().getHits().getTotalHits();
        } finally {
            timerCtxt.stop();
        }
    }

    @Override
    public List<ISearchResult<?>> findPartial(final String inStr, final ESearchArea... areas) throws IOException {
        return findPartial(inStr, 10, areas);
    }

    @Override
    public List<ISearchResult<?>> findPartial(final String inStr, final int inSize, final ESearchArea... areas) throws IOException {
        final List<ISearchResult<?>> results = Lists.newArrayList();
        final Collection<ESearchArea> areasColl = Lists.newArrayList(areas);  // Yuk, only so we can do the remove trick below

        if (areasColl.contains(ESearchArea.TAGS)) {
            // Pretty lame - these will only be *exact* matches, so it/they must go first
            for (ITag each : findTagsByName(inStr)) {
                results.add(new TagSearchResult(each));
            }
        }

        // Yuk
        areasColl.remove(ESearchArea.TAGS);
        if (areasColl.isEmpty()) {
            return results;
        }

        // Yuk
        final String[] typesArr = areasColl.stream().map(inArea -> {
            if (inArea == ESearchArea.ITEMS) {
                return "items";
            } else /* if ( inArea == ESearchArea.RECIPES) */ {
                return "recipes";
            }
        }).toArray(String[]::new);

//      Broken: final SearchResponse resp = esClient.prepareSearch("recipe").setTypes(typesArr).setQuery(queryString("autoCompleteTerms:" + inStr)).setExplain(true).setSize(inSize).execute().actionGet();
        final SearchResponse resp = esClient.prepareSearch("recipe").setTypes(typesArr).setQuery(termQuery("autoCompleteTerms", inStr.toLowerCase())).setSize(inSize).execute().actionGet();
        final SearchHit[] hits = resp.getHits().getHits();

        for (final SearchHit eachHit : hits) {
            if (eachHit.getType().equals("items")) {
                results.add(new ItemSearchResult(mapper.readValue(EsUtils.toBytes(eachHit.getSourceRef()), CanonicalItem.class)));
            } else {
                results.add(new RecipeSearchResult(mapper.readValue(EsUtils.toBytes(eachHit.getSourceRef()), Recipe.class)));
            }
        }

        return results;
    }

    private static class ItemSearchResult implements ISearchResult<ICanonicalItem> {

        private ICanonicalItem item;

        ItemSearchResult(final ICanonicalItem item) {
            this.item = item;
        }

        public long getId() {
            return item.getId();
        }

        @Override
        public String getDisplayName() {
            return item.getCanonicalName();
        }

        public String getType() {
            return "item";
        }

        @JsonIgnore
        @Override
        public ICanonicalItem getEntity() {
            return item;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("itemName", getEntity().getCanonicalName()).toString();
        }
    }

    private static class RecipeSearchResult implements ISearchResult<IRecipe> {

        private IRecipe recipe;

        RecipeSearchResult(final IRecipe recipe) {
            this.recipe = recipe;
        }

        public long getId() {
            return recipe.getId();
        }

        @Override
        public String getDisplayName() {
            return recipe.getTitle();
        }

        public String getType() {
            return "recipe";
        }

        @JsonIgnore
        @Override
        public IRecipe getEntity() {
            return recipe;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("recipeName", getEntity().getTitle()).toString();
        }
    }

    private static class TagSearchResult implements ISearchResult<ITag> {

        private ITag tag;

        TagSearchResult(final ITag tag) {
            this.tag = tag;
        }

        public long getId() {
            return tag.hashCode();
        }

        @Override
        public String getDisplayName() {
            return "'" + TagUtils.formatTagName(tag) + "' tag";
        }

        public String getType() {
            return "tag";
        }

        @JsonIgnore
        @Override
        public ITag getEntity() {
            return tag;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("tag", getDisplayName()).toString();
        }
    }
}