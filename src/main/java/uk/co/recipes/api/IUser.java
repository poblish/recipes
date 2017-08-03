/**
 *
 */
package uk.co.recipes.api;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import uk.co.recipes.api.ratings.IItemRating;
import uk.co.recipes.api.ratings.IRecipeRating;

import java.util.Collection;
import java.util.Set;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IUser extends java.io.Serializable {

    long getId();
    void setId(long id);

    String getUserName();
    String getFirstName();
    String getLastName();
    String getDisplayName();

    String getEmail();
    void setEmail(String email);
    boolean getEmailValidated();

    boolean isActive();
    DateTime getLastLoginTime();

    IUserPreferences getPrefs();

    void addAuth(IUserAuth auth);
    boolean removeAuth(IUserAuth auth);
    Set<IUserAuth> getAuths();

    Optional<IItemRating> addRating(IItemRating inRating);
    Collection<IItemRating> getItemRatings();

    Optional<IRecipeRating> addRating(IRecipeRating inRating);
    Collection<IRecipeRating> getRecipeRatings();

    void addFave(final ICanonicalItem item);
    boolean isFave(final ICanonicalItem item);
    void removeFave(final ICanonicalItem item);
    Collection<ICanonicalItem> getFaveItems();

    void addFave(final IRecipe recipe);
    boolean isFave(final IRecipe recipe);
    void removeFave(final IRecipe recipe);
    Collection<IRecipe> getFaveRecipes();

    Collection<ICanonicalItem> getExcludedItemParents();
    Collection<IRecipe> getExcludedRecipes();
}