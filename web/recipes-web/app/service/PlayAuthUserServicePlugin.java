/**
 * 
 */
package service;

import play.Application;
import play.Logger;
import uk.co.recipes.User;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

/**
 * "The combination of getId and getProvider from AuthUser can be used to identify an user"
 *
 * @author andrewregan
 *
 */
public class PlayAuthUserServicePlugin extends UserServicePlugin {

	public PlayAuthUserServicePlugin( final Application app) {
		super(app);
	}

	/**
	 * The getLocalIdentity function gets called on any login to check whether the session user still has a valid corresponding local
	 * user. Returns the local identifying object if the auth provider/id combination has been linked to a local user account
	 * already or null if not.
	 */
	@Override
	public Object getLocalIdentity( final AuthUserIdentity inUser) {
		Logger.info("getLocalIdentity() for " + inUser);
		return new User("aregan", "Andrew Regan");
	}

	/**
	 * The link function links a new account to an existing local user. Returns the auth user to log in with.
	 */
	@Override
	public AuthUser link( final AuthUser inUser1, final AuthUser inUser2) {
		throw new RuntimeException("unimpl");
	}

	/**
	 * The merge function should merge two different local user accounts to one account. Returns the user to generate the
	 * session information from.
	 */
	@Override
	public AuthUser merge( final AuthUser inUser1, final AuthUser inUser2) {
		throw new RuntimeException("unimpl");
	}

	/**
	 * The update method is called when a user logs in. You might make profile updates here with data coming from the login
	 * provider or bump a last-logged-in date.
	 */
	@Override
	public AuthUser update( final AuthUser inUser) {
		return inUser;
	}

	/**
	 * The save method of the UserServicePlugin is called, when the user logs in for the first time (i.e. getLocalIdentity
	 * returned null for this AuthUser). This method should store the user to the database and return an object identifying the user.
	 */
	@Override
	public Object save( final AuthUser inUser) {
		throw new RuntimeException("unimpl");
	}
}
