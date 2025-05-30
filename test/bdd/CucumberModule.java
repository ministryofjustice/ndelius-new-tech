package bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import play.test.TestBrowser;
import views.ChromeTestBrowser;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class CucumberModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TestBrowser.class).toInstance(ChromeTestBrowser.create(play.api.test.Helpers.testServerPort()));
        bind(WireMockServer.class).annotatedWith(Names.named("pdfWireMock")).toInstance( new WireMockServer(wireMockConfig().port(Ports.PDF.getPort()).jettyStopTimeout(10000L)));
        bind(WireMockServer.class).annotatedWith(Names.named("alfrescofWireMock")).toInstance( new WireMockServer(wireMockConfig().port(Ports.ALFRESCO.getPort()).jettyStopTimeout(10000L)));
        bind(WireMockServer.class).annotatedWith(Names.named("offenderApiWireMock")).toInstance( new WireMockServer(wireMockConfig().port(Ports.OFFENDER_API.getPort()).jettyStopTimeout(10000L)));
    }

}