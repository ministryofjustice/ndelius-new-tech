package views;

import com.mongodb.rx.client.MongoClient;
import interfaces.DocumentStore;
import interfaces.OffenderApi;
import interfaces.PdfGenerator;
import org.mockito.Mock;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithBrowser;

import static org.mockito.Mockito.mock;
import static play.inject.Bindings.bind;

public class WithPartialMockedApplicationBrowser extends WithBrowser {
    @Mock
    protected PdfGenerator pdfGenerator;
    @Mock
    protected OffenderApi offenderApi;
    @Mock
    protected DocumentStore documentStore;
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().
                overrides(
                        bind(PdfGenerator.class).toInstance(pdfGenerator),
                        bind(DocumentStore.class).toInstance(documentStore),
                        bind(OffenderApi.class).toInstance(offenderApi),
                        bind(MongoClient.class).toInstance(mock(MongoClient.class))
                )
                .configure("params.user.token.valid.duration", "100000d")
                .build();
    }

}
