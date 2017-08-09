import com.feth.play.module.pa.Resolver;
import dagger.Module;
import dagger.Provides;
import play.Application;
import play.api.Configuration;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IUserPersistence;


@Module
public /* abstract */ class ApplicationModule {

    @Provides
    public static Application providesApplication(MyComponentsFromContext myComponentsFromContext) {
        return myComponentsFromContext.application();
    }

    @Provides
    public IUserPersistence userPersistence() {
        return new EsUserFactory();
    }

    @Provides
    public static Configuration playConfig() {
        return Configuration.empty();
    }

    @Provides
    public play.api.http.HttpErrorHandler err() {
        return null;  // FIXME
    }

    @Provides
    public controllers.AssetsMetadata assetsMetadata() {
        return null;  // FIXME
    }

    @Provides
    public static Resolver provideAuthResolver() {
        return new MyResolver();
    }
}
