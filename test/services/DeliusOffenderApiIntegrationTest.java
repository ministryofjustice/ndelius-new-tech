package services;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static scala.io.Source.fromInputStream;

public class DeliusOffenderApiIntegrationTest extends WithApplication {
    private OffenderApi offenderApi;
    private static final int PORT = 18080;

    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().port(PORT).jettyStopTimeout(10000L));


    @Before
    public void beforeEach() {

        wireMock.stubFor(
                get(urlEqualTo("/probationAreas/code/N01"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/probationAreaByCode_N01.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/probationAreas/code/N02"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/probationAreaByCode_N02.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/all"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offender.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/all"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offender.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/courtAppearances"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/courtAppearances.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/courtReports/41"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/courtReport.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/offences"))
                        .willReturn(
                                okForContentType("application/json",  loadResource("/deliusoffender/offences.json"))));

        wireMock.stubFor(
            get(urlEqualTo("/offenders/crn/X12345/institutionalReports/999"))
                .willReturn(
                    okForContentType("application/json",  loadResource("/deliusoffender/institutionalReports.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/registrations"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderRegistrations.json"))));

        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/convictions"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderConvictions.json"))));


        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/appointments"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderAppointments.json"))));


        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/personalCircumstances"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderPersonalCircumstances.json"))));



        offenderApi = instanceOf(OffenderApi.class);
    }

    @Test
    public void setsBearerTokenInHeader() {
        offenderApi.getOffenderByCrn("ABC", "X12345").toCompletableFuture().join();

        wireMock.verify(getRequestedFor(anyUrl()).withHeader("Authorization", new EqualToPattern("Bearer ABC")) );
    }

    @Test
    public void getsOffenderByCrn() {
        val offender = offenderApi.getOffenderByCrn("ABC", "X12345").toCompletableFuture().join();

        assertThat(offender.getFirstName()).isEqualTo("John");
        assertThat(offender.getSurname()).isEqualTo("Smith");
        assertThat(offender.getOtherIds()).extracting("pncNumber").asString().contains("2018/123456P");
        assertThat(offender.getContactDetails().mainAddress().get().getStatus().getCode()).isEqualTo("M");
        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/crn/X12345/all")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }

    @Test
    public void getsCourtAppearancesByCrn() {
        val courtAppearances = offenderApi.getCourtAppearancesByCrn("ABC", "X12345").toCompletableFuture().join();

        assertThat(courtAppearances.getItems().size()).isEqualTo(3);
        assertThat(courtAppearances.getItems().get(0).getCourtAppearanceId()).isEqualTo(1);
        assertThat(courtAppearances.getItems().get(0).getCourt().getCourtId()).isEqualTo(1);
        assertThat(courtAppearances.getItems().get(0).getCourtReports().size()).isEqualTo(2);
        assertThat(courtAppearances.getItems().get(0).getCourtReports().get(0).getCourtReportId()).isEqualTo(1);
        assertThat(courtAppearances.getItems().get(0).getOffenceIds().size()).isEqualTo(3);

        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/crn/X12345/courtAppearances")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }

    @Test
    public void getsACourtReportByCrnAndReportId() {
        val courtReport = offenderApi.getCourtReportByCrnAndCourtReportId("ABC", "X12345", "41").toCompletableFuture().join();

        assertThat(courtReport).isNotNull();
        assertThat(courtReport.getDateRequired()).isEqualTo("2018-07-17T00:00:00");
        assertThat(courtReport.getRequiredByCourt().getCourtName()).isEqualTo("Mansfield  Magistrates Court");

        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/crn/X12345/courtReports/41")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }

    @Test
    public void getsOffencesByCrn() {
        val offences = offenderApi.getOffencesByCrn("ABC", "X12345").toCompletableFuture().join();

        assertThat(offences.getItems().size()).isEqualTo(3);
        assertThat(offences.getItems().get(0).getOffenceId()).isEqualTo("M1");
        assertThat(offences.getItems().get(0).getDetail().getCode()).isEqualTo("00101");

        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/crn/X12345/offences")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }

    private static Map.Entry<String, String> entry(String code, String description) {
        return new AbstractMap.SimpleEntry<>(code, description);
    }

    private static String loadResource(String resource) {
        return fromInputStream(new Environment(Mode.TEST).resourceAsStream(resource), "UTF-8").mkString();
    }


    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure("offender.api.url", String.format("http://localhost:%d/", PORT))
                .build();
    }


}
