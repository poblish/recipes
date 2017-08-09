package service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.google.common.base.Optional;
import play.Logger;
import play.mvc.Http.Session;
import uk.co.recipes.UserAuth;
import uk.co.recipes.api.IUser;
import uk.co.recipes.metrics.MetricNames;
import uk.co.recipes.service.api.IUserPersistence;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Service layer for User DB entity
 */
public class UserProvider {

    private final IUserPersistence users;
    private final PlayAuthenticate auth;
    private final MetricRegistry metrics;

    @Inject
    public UserProvider(final PlayAuthenticate auth, final IUserPersistence users, final MetricRegistry inMetrics) {
        this.auth = checkNotNull(auth);
        this.users = checkNotNull(users);
        this.metrics = checkNotNull(inMetrics);
    }

    @Nullable
    public IUser getUser(final Session session) {
        try (Timer.Context ignored = metrics.timer( MetricNames.TIMER_USER_LOCAL_GET ).time()) {
            return getUntimedLocalUser(session);
        }
    }

    private IUser getUntimedLocalUser(final Session session) {
        try {
            final AuthUser currentAuthUser = auth.getUser(session);

            if (Logger.isTraceEnabled()) {
                Logger.trace("currentAuthUser: " + currentAuthUser);
            }

            if ( currentAuthUser == null) {
                return null;
            }

            final Optional<IUser> theUser = users.findWithAuth( new UserAuth( currentAuthUser.getProvider(), currentAuthUser.getId() ) );

            if (Logger.isTraceEnabled()) {
                Logger.trace("theUser: " + theUser);
            }

            return theUser.orNull();
        }
        catch (IOException e) {
            Logger.error("ERROR: " + e);
            return null;
        }
    }
}
