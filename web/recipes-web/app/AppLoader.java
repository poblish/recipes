import play.Application;
import play.ApplicationLoader;

public class AppLoader implements ApplicationLoader {

    @Override
    public Application load(Context context) {
        ApplicationComponent applicationComponent = DaggerApplicationComponent.builder()
                .context(context)
                .build();

        return applicationComponent.application();
    }
}