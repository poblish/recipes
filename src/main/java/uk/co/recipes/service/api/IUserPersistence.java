package uk.co.recipes.service.api;

import java.io.IOException;

import uk.co.recipes.api.IUser;
import uk.co.recipes.api.IUserAuth;

import com.google.common.base.Optional;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IUserPersistence extends IPersistenceAPI<IUser> {

	Optional<IUser> findWithAuth( final IUserAuth inAuth) throws IOException;
}