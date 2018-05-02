package services;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableList;
import interfaces.OffenderApi;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Application;
import play.Environment;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.AbstractMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static scala.io.Source.fromInputStream;

public class DeliusOffenderApiIntegrationTest extends WithApplication {
    private OffenderApi offenderApi;
    private static final int PORT = 18080;


    @Rule
    public WireMockRule wireMock = new WireMockRule(PORT);


    @Before
    public void beforeEach() {
        val n01 = fromInputStream(new Environment(Mode.TEST).resourceAsStream("/deliusoffender/probationAreaByCode_N01.json"), "UTF-8").mkString();
        val n02 = fromInputStream(new Environment(Mode.TEST).resourceAsStream("/deliusoffender/probationAreaByCode_N02.json"), "UTF-8").mkString();

        wireMock.stubFor(
                get(urlEqualTo("/probationAreas/code/N01"))
                        .willReturn(
                                okForContentType("application/json",  n01)));

        wireMock.stubFor(
                get(urlEqualTo("/probationAreas/code/N02"))
                        .willReturn(
                                okForContentType("application/json",  n02)));

        offenderApi = instanceOf(OffenderApi.class);
    }


    @Test
    public void singleProbationAreaCodeMakesSingleAPICall() {
        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01")).toCompletableFuture().join();

        wireMock.verify(getRequestedFor(urlEqualTo("/probationAreas/code/N01")));
    }

    @Test
    public void multipleProbationAreaCodesMakesMultipleAPICall() {
        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01", "N02")).toCompletableFuture().join();

        wireMock.verify(getRequestedFor(urlEqualTo("/probationAreas/code/N01")));
        wireMock.verify(getRequestedFor(urlEqualTo("/probationAreas/code/N02")));
    }

    @Test
    public void descriptionsAreCached() {
        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01")).toCompletableFuture().join();

        wireMock.verify(1, getRequestedFor(urlEqualTo("/probationAreas/code/N01")));

        wireMock.resetRequests();

        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01", "N02")).toCompletableFuture().join();
        wireMock.verify(0, getRequestedFor(urlEqualTo("/probationAreas/code/N01")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/probationAreas/code/N02")));

        wireMock.resetRequests();

        val probationAreaCodeToDescriptionMap = offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01", "N02")).toCompletableFuture().join();

        wireMock.verify(0, getRequestedFor(anyUrl()) );

        assertThat(probationAreaCodeToDescriptionMap)
                .contains(entry("N01", "NPS North West"))
                .contains(entry("N02", "NPS North East"));

    }

    @Test
    public void doesNotBotherCallingAPIWhenEmptyListSupplied() {
        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of()).toCompletableFuture().join();

        wireMock.verify(0, getRequestedFor(anyUrl()) );
    }

    @Test
    public void setsBearerTokenInHeader() {
        offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01")).toCompletableFuture().join();

        wireMock.verify(getRequestedFor(anyUrl()).withHeader("Authorization", new EqualToPattern("Bearer ABC")) );
    }


    @Test
    public void returnsMapOfCodeDescriptions() {
        val probationAreaCodeToDescriptionMap = offenderApi.probationAreaDescriptions("ABC", ImmutableList.of("N01", "N02")).toCompletableFuture().join();

        assertThat(probationAreaCodeToDescriptionMap)
                .contains(entry("N01", "NPS North West"))
                .contains(entry("N02", "NPS North East"));
    }

    private static Map.Entry<String, String> entry(String code, String description) {
        return new AbstractMap.SimpleEntry<>(code, description);
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure("offender.api.url", String.format("http://localhost:%d/", PORT))
                .build();
    }


}
