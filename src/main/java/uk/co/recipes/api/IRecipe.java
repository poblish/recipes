/**
 *
 */
package uk.co.recipes.api;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IRecipe extends ITagging, Cloneable, Serializable {

    long getId();
    void setId(long id);

    String getTitle();
    void setTitle(String title);

    Locale getLocale();

    IUser getCreator();
    OffsetDateTime getCreationTime();

    Object clone();

    IForkDetails getForkDetails();
    void setForkDetails(final IForkDetails inForkDetails);

    Collection<IIngredient> getIngredients();
    Collection<IIngredient> getSortedIngredients();

    Collection<ICanonicalItem> getItems();
    boolean containsItem(final ICanonicalItem item);

    List<IRecipeStage> getStages();

    boolean removeItems(final ICanonicalItem... inItems);
    boolean addIngredients(final IIngredient... inIngredients);
    boolean removeIngredients(final IIngredient... inIngredients);

    void addTags(final Map<ITag,Serializable> tags);
}
