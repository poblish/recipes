/**
 * 
 */
package uk.co.recipes.service.api;

import java.io.IOException;
import java.util.List;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.ITag;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ISearchAPI {

	List<ICanonicalItem> findItemsByName( final String inName) throws IOException;
	int countItemsByName( final String inName);

	List<IRecipe> findRecipesByName( final String inName) throws IOException;
	List<IRecipe> findRecipesByName( final String inName, int inSize) throws IOException;
	int countRecipesByName( final String inName);

	List<ITag> findTagsByName( final String inName);
	int countTagsByName( final String inName);

    List<ICanonicalItem> findItemsByTag( final ITag inTag) throws IOException;
    List<ICanonicalItem> findItemsByTag( final ITag inTag, final String value) throws IOException;
    int countItemsByTag( final ITag inTag);

//    List<IRecipe> findRecipesByTag( final ITag inTag) throws IOException;
//    List<IRecipe> findRecipesByTag( final ITag inTag, final String value) throws IOException;
    List<IRecipe> findRandomRecipesByTag( int inCount, final ITag inTag) throws IOException;
    int countRecipesByTag( final ITag inTag);

    long[] findRecipeIdsByTag( final ITag inTag);
    long[] findRecipeIdsByTag( final ITag inTag, final String value);

    long[] findRecipeIdsByItemName( final String... inName);
    long[] findRecipeIdsByItemName( final ICanonicalItem... inName);
    List<IRecipe> findRecipesByItemName( final String... inName) throws IOException;
    List<IRecipe> findRecipesByItemName( final ICanonicalItem... inName) throws IOException;
    List<IRecipe> findRandomRecipesByItemName( final int inCount, final ICanonicalItem... inName) throws IOException;
    int countRecipesByItemName( final String... inName);

    List<ISearchResult<?>> findPartial( final String inStr, final ESearchArea... areas) throws IOException;
    List<ISearchResult<?>> findPartial( final String inStr, final int inSize, final ESearchArea... areas) throws IOException;
}
