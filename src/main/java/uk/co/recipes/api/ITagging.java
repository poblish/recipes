/**
 * 
 */
package uk.co.recipes.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface ITagging {

	void addTag( final ITag key);
	void addTag( final ITag key, final Serializable value);

	Map<ITag,Serializable> getTags();
	List<String> getTagNamesForDisplay();
}