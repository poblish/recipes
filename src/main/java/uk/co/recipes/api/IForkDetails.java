/**
 * 
 */
package uk.co.recipes.api;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public interface IForkDetails {

	long getOriginalId();
	String getOriginalTitle();
	IUser getOriginalUser();
}
