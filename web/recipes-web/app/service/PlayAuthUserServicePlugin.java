package service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.AbstractUserService;
import com.feth.play.module.pa.user.*;
import com.google.common.base.Optional;
import play.Logger;
import uk.co.recipes.User;
import uk.co.recipes.UserAuth;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.IUserPersistence;

import javax.inject.Inject;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * "The combination of getId and getProvider from AuthUser can be used to identify an user"
 *
 * @author andrewregan
 *
 */
public class PlayAuthUserServicePlugin extends AbstractUserService {

	private final IUserPersistence users;

	@Inject
	public PlayAuthUserServicePlugin(final IUserPersistence users, final PlayAuthenticate auth) {
		super(auth);
		this.users = checkNotNull(users);
	}

	/**
	 * The getLocalIdentity function gets called on any login to check whether the session user still has a valid corresponding local
	 * user. Returns the local identifying object if the auth provider/id combination has been linked to a local user account
	 * already or null if not.
	 */
	@Override
	public Object getLocalIdentity( final AuthUserIdentity inUser) {
		try {
			final Optional<IUser> theUser = users.findWithAuth( new UserAuth( inUser.getProvider(), inUser.getId() ) );
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
            if ( name != null && !name.isEmpty()) {
                newUser.setDisplayName(name);
            }
        }

        if (inUser instanceof FirstLastNameIdentity) {
            final FirstLastNameIdentity identity = (FirstLastNameIdentity) inUser;
            final String firstName = identity.getFirstName();
            final String lastName = identity.getLastName();
            if ( firstName != null && !firstName.isEmpty()) {
                newUser.setFirstName(firstName);
            }
            if ( lastName != null && !lastName.isEmpty()) {
                newUser.setLastName(lastName);
            }
        }

		try {
			Logger.info("*** Try to save: " + inUser + " with " + newUser);

			users.put( newUser, /* Yuk: */ users.toStringId(newUser));
			users.waitUntilRefreshed();  // Yuk, but if we don't do this, we might redirect to page + try to show Login details before User exists in index

			return newUser;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
