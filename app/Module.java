import com.google.inject.AbstractModule;
import com.mongodb.rx.client.MongoClient;
import injection.DocumentStoreProvider;
import injection.MongoClientProvider;
import injection.OffenderApiProvider;
import injection.RestClientBuilderProvider;
import injection.RestHighLevelClientProvider;
import interfaces.DocumentStore;
import interfaces.OffenderApi;
import interfaces.PdfGenerator;
import interfaces.UserAwareApiToken;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import services.RestPdfGenerator;
import services.UserAwareAuthenticationApi;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule {

    @Override
    public void configure() {

        bind(PdfGenerator.class).to(RestPdfGenerator.class);
        bind(UserAwareApiToken.class).to(UserAwareAuthenticationApi.class);

        bind(DocumentStore.class).toProvider(DocumentStoreProvider.class);
        bind(OffenderApi.class).toProvider(OffenderApiProvider.class);
        bind(RestClientBuilder.class).toProvider(RestClientBuilderProvider.class);
        bind(RestHighLevelClient.class).toProvider(RestHighLevelClientProvider.class);

        bind(MongoClient.class).toProvider(MongoClientProvider.class).asEagerSingleton();
    }
}
