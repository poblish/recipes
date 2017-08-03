/**
 *
 */
package uk.co.recipes.api;

import org.joda.time.DateTime;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IForkDetails {

    long getOriginalId();
    String getOriginalTitle();
    IUser getOriginalUser();
    DateTime getForkTime();
}
