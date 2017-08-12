package uk.co.recipes;

import com.google.common.base.Throwables;
import dagger.Module;
import dagger.Provides;
import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;

import javax.inject.Singleton;
import java.io.IOException;

@Module
public class ProductionMyrrixModule {

    @Provides
    @Singleton
    ClientRecommender provideClientRecommender() {
        final MyrrixClientConfiguration clientConfig = new MyrrixClientConfiguration();
        clientConfig.setHost("localhost");
        clientConfig.setPort(8080);

        // TranslatingClientRecommender recommender = new TranslatingClientRecommender( new ClientRecommender(clientConfig) );
        try {
            return new ClientRecommender(clientConfig);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
