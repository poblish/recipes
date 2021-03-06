import com.feth.play.module.pa.controllers.Authenticate;
import play.ApplicationLoader;
import play.BuiltInComponentsFromContext;
import play.api.routing.Router;
import play.components.BodyParserComponents;
import play.controllers.AssetsComponents;
import play.data.FormFactoryComponents;
import play.filters.components.NoHttpFiltersComponents;
import router.Routes;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;

public class MyComponentsFromContext extends BuiltInComponentsFromContext implements NoHttpFiltersComponents,
        AssetsComponents,
        FormFactoryComponents,
        BodyParserComponents {

    @Inject controllers.Application appController;
    @Inject controllers.Items itemsController;
    @Inject controllers.Recipes  recipesController;
    @Inject controllers.Search searchController;
    @Inject controllers.Tags tagsController;
    @Inject controllers.Users usersController;
    @Inject Authenticate auth;

    @Inject
    public MyComponentsFromContext(ApplicationLoader.Context context) {
        super(context);
    }

    @Override
    public play.routing.Router router() {
        Router routes = new Routes( scalaHttpErrorHandler(), appController, itemsController, recipesController, searchController, tagsController, usersController, auth, assets());
        return routes.asJava();
    }

    @Override
    public ExecutionContext executionContext() {
        return actorSystem().dispatcher();
    }
}
