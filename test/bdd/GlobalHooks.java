package bdd;

import bdd.wiremock.AlfrescoStoreMock;
import bdd.wiremock.OffenderApiMock;
import bdd.wiremock.PdfGeneratorMock;
import com.mongodb.rx.client.MongoClient;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.elasticsearch.client.RestHighLevelClient;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.TestBrowser;
import views.WithChromeBrowser;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static play.inject.Bindings.bind;

public class GlobalHooks extends WithChromeBrowser {
    @Inject
    private TestBrowser theTestBrowser;

    @Inject
    private AlfrescoStoreMock alfrescoStoreMock;
    @Inject
    private PdfGeneratorMock pdfGeneratorMock;
    @Inject
    private OffenderApiMock offenderApiMock;

    @Before
    public void before() {
        startServer();
        pdfGeneratorMock.start().stubDefaults();
        offenderApiMock.start().stubDefaults();
        alfrescoStoreMock.start().stubDefaults();

        createBrowser();
    }

    @After
    public void after() {
        pdfGeneratorMock.stop();
        alfrescoStoreMock.stop();
        offenderApiMock.stop();
        stopServer();
        quitBrowser();
    }

    @Override
    protected TestBrowser provideBrowser(int port) {
        theTestBrowser.getConfiguration().setBaseUrl("http://localhost:" + port);
        return theTestBrowser;
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().
                overrides(
                        bind(RestHighLevelClient.class).toInstance(mock(RestHighLevelClient.class)),
                        bind(MongoClient.class).toInstance(mock(MongoClient.class))
                )
                .configure("params.user.token.valid.duration", "100000d")
                .configure("pdf.generator.url", String.format("http://localhost:%d/", Ports.PDF.getPort()))
                .configure("store.alfresco.url", String.format("http://localhost:%d/", Ports.ALFRESCO.getPort()))
                .configure("offender.api.url", String.format("http://localhost:%d/", Ports.OFFENDER_API.getPort()))
                .build();
    }

}
