/**
 * 
 */
package service;

import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import java.io.IOException;
import play.Application;
import play.Logger;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.User;
import uk.co.recipes.UserAuth;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IUserPersistence;
import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import dagger.Module;
import dagger.ObjectGraph;

/**
 * "The combination of getId and getProvider from AuthUser can be used to identify an user"
 *
 * @author andrewregan
 *
 */
public class PlayAuthUserServicePlugin extends UserServicePlugin {

	private static final ObjectGraph GRAPH = ObjectGraph.create( new RecipesWebAppModule() );  // FIXME Yuk!
    private static final IUserPersistence USERS = GRAPH.get( EsUserFactory.class );  // FIXME Yuk!


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
		try {
			final Optional<IUser> theUser = USERS.findWithAuth( new UserAuth( inUser.getProvider(), inUser.getId() ) );
			Logger.info("*** Loaded " + theUser);
			return theUser.orNull();
		}
		catch (IOException e) {
			Logger.error("ERROR: " + e);
			return null;
		}
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
		Logger.info("*** Update for " + inUser);
		return inUser;
	}

	/**
	 * The save method of the UserServicePlugin is called, when the user logs in for the first time (i.e. getLocalIdentity
	 * returned null for this AuthUser). This method should store the user to the database and return an object identifying the user.
	 */
	@Override
	public Object save( final AuthUser inUser) {
		final User newUser = new User( inUser.getId(), inUser.getId());
		newUser.addAuth( new UserAuth( inUser.getProvider(), inUser.getId() ) );

		newUser.initLastLoginTime();

        if (inUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) inUser;
            // Remember, even when getting them from FB & Co., emails should be verified within the application as a
            // security breach there might break your security as well!
            final String emailStr = identity.getEmail();
            if ( emailStr != null && !emailStr.isEmpty()) {
                newUser.setEmail(emailStr);
            }
        }

        if (inUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) inUser;
            final String name = identity.getName();
            if (name != null) {
//                newUser.name = name;
            }
        }

        if (inUser instanceof FirstLastNameIdentity) {
            final FirstLastNameIdentity identity = (FirstLastNameIdentity) inUser;
            final String firstName = identity.getFirstName();
            final String lastName = identity.getLastName();
            if ( firstName != null && !firstName.isEmpty()) {
                newUser.setFirstName(lastName);
            }
            if ( lastName != null && !lastName.isEmpty()) {
                newUser.setLastName(lastName);
            }
        }

		try {
			Logger.info("*** Try to save: " + inUser + " with " + newUser);
			USERS.put( newUser, null);
			return newUser;
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Module( includes=DaggerModule.class, injects={})
	static class RecipesWebAppModule {
		
	}
}
