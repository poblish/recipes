/**
 *
 */
package uk.co.recipes.api;

import java.time.OffsetDateTime;

/**
 * TODO
 *
 * @author andrewregan
 */
public interface IForkDetails {

    long getOriginalId();
    String getOriginalTitle();
    IUser getOriginalUser();
    OffsetDateTime getForkTime();
}
