import controllers.Assets;
import controllers.AssetsComponents;
import play.Application;
import play.ApplicationLoader;
import play.BuiltInComponentsFromContext;
import play.filters.components.HttpFiltersComponents;
import play.routing.Router;

import javax.inject.Inject;

public class AppLoader implements ApplicationLoader {

//    @Inject
//    controllers.Application myApp;

    @Override
    public Application load(Context context) {
        return new MyComponents(context).application();
    }

    public class MyComponents extends BuiltInComponentsFromContext implements HttpFiltersComponents { // FIXME: }, AssetsComponents {

        public MyComponents(ApplicationLoader.Context context) {
            super(context);
        }

        @Override
        public Router router() {
//            controllers.Application homeController = new controllers.Application();
            //Assets assets = new Assets(scalaHttpErrorHandler(), assetsMetadata());
            return new router.Routes().asJava(); //scalaHttpErrorHandler(), null /* homeController */, myApp /* assets */).asJava();
        }
    }}