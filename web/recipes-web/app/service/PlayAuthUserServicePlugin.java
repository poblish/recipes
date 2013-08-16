/**
 * 
 */
package service;

import play.Application;
import uk.co.recipes.User;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class PlayAuthUserServicePlugin extends UserServicePlugin {

	public PlayAuthUserServicePlugin( final Application app) {
		super(app);
	}

	/* (non-Javadoc)
	 * @see com.feth.play.module.pa.service.UserService#getLocalIdentity(com.feth.play.module.pa.user.AuthUserIdentity)
	 */
	@Override
	public Object getLocalIdentity( final AuthUserIdentity inUser) {
		return new User("aregan", "Andrew Regan");
	}

	/* (non-Javadoc)
	 * @see com.feth.play.module.pa.service.UserService#link(com.feth.play.module.pa.user.AuthUser, com.feth.play.module.pa.user.AuthUser)
	 */
	@Override
	public AuthUser link( final AuthUser inUser1, final AuthUser inUser2) {
		throw new RuntimeException("unimpl");
	}

	/* (non-Javadoc)
	 * @see com.feth.play.module.pa.service.UserService#merge(com.feth.play.module.pa.user.AuthUser, com.feth.play.module.pa.user.AuthUser)
	 */
	@Override
	public AuthUser merge( final AuthUser inUser1, final AuthUser inUser2) {
		throw new RuntimeException("unimpl");
	}

	/* (non-Javadoc)
	 * @see com.feth.play.module.pa.service.UserService#save(com.feth.play.module.pa.user.AuthUser)
	 */
	@Override
	public Object save( final AuthUser inUser) {
		throw new RuntimeException("unimpl");
	}
}
