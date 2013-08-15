import play.Application;
import play.GlobalSettings;
import play.Logger;
import uk.co.recipes.DaggerModule;
import dagger.Module;
import dagger.ObjectGraph;

/**
 * TODO
 * 
 * @author andrewregan
 * 
 */
public class Global extends GlobalSettings {

	private static ObjectGraph graph = ObjectGraph.create( new RecipesWebAppModule() );

	@Override
	public void onStart( final Application app) {
		Logger.info("Application has started");
	}

	@Override
	public void onStop( final Application app) {
		Logger.info("Application shutdown...");
	}

	@Override
	public <T> T getControllerInstance( final Class<T> inClass) {
		return graph.get(inClass);
	}

	@Module( includes=DaggerModule.class, injects={controllers.Application.class, controllers.Recipes.class, controllers.Search.class}, overrides=true)
	static class RecipesWebAppModule {
		
	}
}