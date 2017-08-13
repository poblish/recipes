import dagger.BindsInstance;
import dagger.Component;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.ProductionMyrrixModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = { DaggerModule.class, ProductionMyrrixModule.class, ApplicationModule.class})
public interface ApplicationComponent {
    play.Application application();

    @Component.Builder
    interface Builder {
        @BindsInstance Builder context(play.ApplicationLoader.Context context);

        ApplicationComponent build();
    }
}
