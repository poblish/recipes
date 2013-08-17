package uk.co.recipes.service.api;

import uk.co.recipes.api.IUser;

import com.google.common.base.Optional;

/**
 * TODO
 * 
 * @author andrewr
 *
 */
public interface IUserPersistence extends IPersistenceAPI<IUser> {

	Optional<IUser> findWithAuth( final String inProvider, final String inId);
}