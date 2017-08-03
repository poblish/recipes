package uk.co.recipes.service.api;

import com.google.common.base.Optional;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.IUserAuth;

import java.io.IOException;

/**
 * TODO
 *
 * @author andrewr
 */
public interface IUserPersistence extends IPersistenceAPI<IUser> {

    IUser adminUser();

    Optional<IUser> findWithAuth(final IUserAuth inAuth) throws IOException;
}