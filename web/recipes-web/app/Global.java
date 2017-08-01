import com.feth.play.module.pa.Resolver;
import com.google.common.collect.Maps;
import dagger.Provides;
import play.Application;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.mvc.Call;
import uk.co.recipes.DaggerModule;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;
import dagger.Module;
import dagger.ObjectGraph;

import javax.inject.Singleton;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class Global /* extends GlobalSettings */ {

	private static ObjectGraph graph = ObjectGraph.create( new RecipesWebAppModule() );

//	@Override
	public void onStart( final Application app) {
		Logger.info("Application has started");

        /* PlayAuthenticate.setResolver(new Resolver() {

            @Override
            public Call login() {
                return / FIXME / routes.Application.index();
            }

            @Override
            public Call afterAuth() {
                // The user will be redirected to this page after authentication if no original URL was saved
                return / FIXME / routes.Application.index();
            }

            @Override
            public Call afterLogout() {
                return / FIXME / routes.Application.index();
            }

            @Override
            public Call auth(final String provider) {
                // You can provide your own authentication implementation, however the default should be sufficient for most cases
            	if (Logger.isTraceEnabled()) {
            		Logger.trace("*** Calling Auth for '" + provider + "'");
            	}
                return com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return routes.Application.oAuthDenied(((AccessDeniedException) e).getProviderKey());
                }

                // more custom problem handling here...

                return super.onException(e);
            }

            @Override
            public Call askLink() {
                // We don't support moderated account linking in this sample.
                // See the play-authenticate-usage project for an example
                return null;
            }

            @Override
            public Call askMerge() {
                // We don't support moderated account merging in this sample.
                // See the play-authenticate-usage project for an example
                return null;
            }
        });
*/
    }

	// @Override
	public void onStop( final Application app) {
		Logger.info("Application shutdown...");
	}

	// @Override
	public <T> T getControllerInstance( final Class<T> inClass) {
		return graph.get(inClass);
	}

	@SuppressWarnings("unchecked")
	// @Override
	public <T extends EssentialFilter> Class<T>[] filters() {
		return new Class[]{ GzipFilter.class };
	}

    @Module( includes=DaggerModule.class, injects={controllers.Application.class, controllers.Recipes.class,
												   controllers.Search.class, controllers.Tags.class,
												   controllers.Users.class, controllers.Items.class})
	static class RecipesWebAppModule {

		@Provides
		@Singleton
		play.Configuration provideConfig() {
			return new play.Configuration(Maps.newHashMap());  // FIXME
		}

		@Provides
		@Singleton
		com.feth.play.module.pa.Resolver provideResolver() {
			return new Resolver() {
				@Override
				public Call login() {
					return null;
				}

				@Override
				public Call afterAuth() {
					return null;
				}

				@Override
				public Call auth(String provider) {
					return null;
				}

				@Override
				public Call askMerge() {
					return null;
				}

				@Override
				public Call askLink() {
					return null;
				}

				@Override
				public Call afterLogout() {
					return null;
				}
			};
		}

	}
}