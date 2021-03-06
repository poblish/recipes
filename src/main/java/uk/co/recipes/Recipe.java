/**
 *
 */
package uk.co.recipes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import uk.co.recipes.api.*;
import uk.co.recipes.tags.RecipeTags;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.co.recipes.tags.TagUtils.findActivated;
import static uk.co.recipes.tags.TagUtils.tagNamesTitleCase;

/**
 * TODO
 *
 * @author andrewregan
 */
public class Recipe implements IRecipe {

    private static final long serialVersionUID = 1L;

    private static final long UNSET_ID = 0x3FFFFFFFFFFFFFFFL;  // Halfway to Long.MAX_VALUE
    public static final long BASE_ID = UNSET_ID + 1L;

    private long id = UNSET_ID;

    private IUser creator;
    private String title;
    private Locale locale;
    private OffsetDateTime creationTime = OffsetDateTime.now();
    private IForkDetails forkDetails;

    private final List<IRecipeStage> stages = Lists.newArrayList();
    private Map<ITag,Serializable> tags = new TreeMap<>(Ordering.usingToString());  // Try to keep the order regular. This will *not* sort enums by name, only by index

    // Purely for Jackson deserialization
    public Recipe() {
    }

    public Recipe(final IUser inCreator, String inTitle, final Locale inLocale) {
        creator = checkNotNull(inCreator, "Creator cannot be null");
        title = checkNotNull(inTitle, "Title cannot be null");
        locale = checkNotNull(inLocale, "Locale cannot be null");
    }

    public Recipe(final IUser inCreator, String inTitle, final Locale inLocale, final OffsetDateTime inCreationTime) {
        this(inCreator, inTitle, inLocale);
        creationTime = checkNotNull(inCreationTime, "CreationTime cannot be null");
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(final String inTitle) {
        title = checkNotNull(inTitle, "Title cannot be null");
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.api.IRecipe#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    public void addStage(final IRecipeStage inStage) {
        stages.add(inStage);
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.api.IRecipe#ingredients()
     */
    @JsonIgnore
    @Override
    public Collection<IIngredient> getIngredients() {
        final Collection<IIngredient> is = Sets.newLinkedHashSet();

        for (IRecipeStage eachStage : stages) {
            is.addAll(eachStage.getIngredients());
        }

        return is;
    }

    @JsonIgnore
    @Override
    public Collection<IIngredient> getSortedIngredients() {
        List<IIngredient> itemsList = Lists.newArrayList(getIngredients());
        itemsList.sort((o1, o2) -> o1.getItem().getCanonicalName().compareToIgnoreCase(o2.getItem().getCanonicalName()));

        return itemsList;
    }

    @Override
    public boolean containsItem(final ICanonicalItem item) {
        for (IRecipeStage eachStage : stages) {
            if (eachStage.containsItem(item)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    @Override
    public Collection<ICanonicalItem> getItems() {
        final Collection<ICanonicalItem> is = Sets.newLinkedHashSet();

        for (IRecipeStage eachStage : stages) {
            is.addAll(eachStage.getItems());
        }

        return is;
    }

    @JsonIgnore
    public Collection<ICanonicalItem> getSortedItems() {
        List<ICanonicalItem> itemsList = Lists.newArrayList(getItems());
        itemsList.sort((o1, o2) -> o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName()));

        return itemsList;
    }

    public boolean containsAllOf(final ICanonicalItem... inOthers) {
        for (final ICanonicalItem eachInclusion : inOthers) {
            boolean found = false;
            for (ICanonicalItem eachItem : getItems()) {
                if (eachItem.descendsFrom(eachInclusion)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.api.IRecipe#stages()
     */
    @Override
    public List<IRecipeStage> getStages() {
        return stages;
    }

    @Override
    public void addTag(final ITag key, final Serializable value) {
        tags.put(key, checkNotNull(value, "Value cannot be null"));
    }

    @Override
    public void addTags(final Map<ITag,Serializable> tags) {
        for (Entry<ITag,Serializable> each : tags.entrySet()) {
            addTag(each.getKey(), each.getValue());
        }
    }

    @Override
    public void addTag(final ITag key) {
        tags.put(key, Boolean.TRUE);
    }

    @Override
    public Map<ITag,Serializable> getTags() {
        return tags;
    }

    @JsonIgnore  // Prevent Jackson insanity
    @Override
    public List<String> getTagNamesForDisplay() {
        return FluentIterable.from(getTags().entrySet()).filter(findActivated()).transform(tagNamesTitleCase()).toList();
    }

    // FIXME Copy/paste from CanonicalItem
    // Jackson *will* use this to persist 'tags'. *Can* be private
    @SuppressWarnings("unused")
    private void setTags(Map<ITag,Serializable> inTags) {
        tags.clear();

        for (Entry<ITag,Serializable> each : inTags.entrySet()) {
            if (each.getValue().equals("true")) /* Ugh!!! */ {
                tags.put(each.getKey(), Boolean.TRUE);
            } else {
                tags.put(each.getKey(), each.getValue());
            }
        }
    }

    @JsonProperty("catsFacet")
    public Set<String> getCategoriesForFacet() {
        return getTagValuesForFacet(RecipeTags.RECIPE_CATEGORY);
    }

    @JsonProperty("cuisineFacet")
    public Set<String> getCuisineForFacet() {
        return getTagValuesForFacet(RecipeTags.RECIPE_CUISINE);
    }

    private Set<String> getTagValuesForFacet(final ITag inTag) {
        final Set<String> tagValues = Sets.newHashSet();

        for (Entry<ITag,Serializable> eachEntry : getTags().entrySet()) {
            if (eachEntry.getKey() == inTag) {
                tagValues.add(eachEntry.getValue().toString().replace(' ', '_').toLowerCase());  // Thwart tokenizing!
            }
        }

        return tagValues;
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.api.IRecipe#removeItems(uk.co.recipes.api.ICanonicalItem[])
     */
    @Override
    public boolean removeItems(ICanonicalItem... inItems) {
        boolean result = false;
        for (final IRecipeStage each : stages) {
            result |= each.removeItems(inItems);
        }
        return result;
    }

    @Override
    public boolean addIngredients(final IIngredient... inIngredients) {
        boolean result = false;
        for (final IRecipeStage each : stages) {
            result |= each.addIngredients(inIngredients);
        }
        return result;
    }

    @Override
    public boolean removeIngredients(final IIngredient... inIngredients) {
        boolean result = false;
        for (final IRecipeStage each : stages) {
            result |= each.removeIngredients(inIngredients);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see uk.co.recipes.api.IRecipe#getCreator()
     */
    @Override
    public IUser getCreator() {
        return creator;
    }

    @Override
    public IForkDetails getForkDetails() {
        return forkDetails;
    }

    @Override
    public void setForkDetails(final IForkDetails inForkDetails) {
        // Can't check null - probably called by Jackson
        forkDetails = inForkDetails;
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public long getId() {
        return id;
    }

    // Strictly for Jackson only. Must be public
    public Collection<String> getAutoCompleteTerms() {
        return Lists.newArrayList(getTitle());  // Jackson barfs if we use Collections.singletonList()
    }

    @Override
    public void setId(long inId) {
        if (id == UNSET_ID && inId == UNSET_ID) {
            // Let Jackson off...
            return;
        }

        Preconditions.checkArgument(inId >= BASE_ID, "New Id must be >= " + BASE_ID + " [" + inId + "]");
        // Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
        id = inId;
    }

    public Object clone() {
        final Recipe theClone = new Recipe(creator, title, locale, creationTime);
        for (IRecipeStage eachStage : stages) {
            theClone.addStage(eachStage);
        }
        for (Entry<ITag,Serializable> eachTag : tags.entrySet()) {
            theClone.addTag(eachTag.getKey(), eachTag.getValue());
        }

        theClone.setForkDetails(getForkDetails());

        return theClone;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(title, locale, stages, tags, creator, /* creationTime, */ forkDetails);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Recipe)) {
            return false;
        }

        final Recipe other = (Recipe) obj;
        return equal(title, other.title) && equal(locale, other.locale) &&
                equal(stages, other.stages) && equal(tags, other.tags) &&
                equal(creator, other.creator) && /* Objects.equal( creationTime, other.creationTime) && */
                equal(forkDetails, other.forkDetails);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("title", title)
                .add("id", (id == UNSET_ID) ? "NEW" : Long.valueOf(id))
                .add("creator", creator)
                .add("fork", forkDetails)
                .add("stages", stages)
                .add("tags", getTags())
                .add("locale", locale)
                .toString();
    }
}
