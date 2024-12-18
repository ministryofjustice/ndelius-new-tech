package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
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

    @Test
    public void getsOffenderByCrn() {
        val offender = offenderApi.getOffenderByCrn("ABC", "X12345").toCompletableFuture().join();

        assertThat(offender.getFirstName()).isEqualTo("John");
        assertThat(offender.getSurname()).isEqualTo("Smith");
        assertThat(offender.getOtherIds()).extracting("pncNumber").contains("2018/123456P");
        assertThat(offender.getContactDetails().mainAddress().get().getStatus().getCode()).isEqualTo("M");
        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/crn/X12345/all")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }

    @Test
    public void getsOffenderDetailByOffenderId() {
        val offender = ObjectNode.class.cast(offenderApi.getOffenderDetailByOffenderId("ABC", "12345").toCompletableFuture().join());

        assertThat(offender.get("firstName").asText()).isEqualTo("John");
        assertThat(offender.get("surname").asText()).isEqualTo("Smith");
        assertThat(offender.get("otherIds").get("pncNumber").asText()).isEqualTo("2018/123456P");
        wireMock.verify(getRequestedFor(urlEqualTo("/offenders/offenderId/12345/all")).withHeader("Authorization", new EqualToPattern("Bearer ABC")));
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

    @Test
    public void getsInstitutionalReports() {
        val institutionalReport = offenderApi.getInstitutionalReport("ABC", "X12345", "999").toCompletableFuture().join();

        assertThat(institutionalReport.getConviction().mainOffenceDescription()).isEqualTo("Fraud subcategory description - 02/11/2018");
    }

    @Test
    public void getsOffenderRegistrationsByOffenderId() {
        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/registrations"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderRegistrations.json"))));


        val registrations = ArrayNode.class.cast(offenderApi.getOffenderRegistrationsByOffenderId("ABC", "12345").toCompletableFuture().join());

        assertThat(registrations.size()).isEqualTo(13);
        wireMock.verify(
                getRequestedFor(
                        urlEqualTo("/offenders/offenderId/12345/registrations"))
                        .withHeader("Authorization", new EqualToPattern("Bearer ABC")));
    }


    @Test
    public void getsOffenderConvictionsByOffenderId() {
        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/convictions"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderConvictions.json"))));


        val convictions = ArrayNode.class.cast(offenderApi.getOffenderConvictionsByOffenderId("ABC", "12345").toCompletableFuture().join());

        assertThat(convictions.size()).isEqualTo(20);
        wireMock.verify(
                getRequestedFor(
                        urlEqualTo("/offenders/offenderId/12345/convictions"))
                        .withHeader("Authorization", new EqualToPattern("Bearer ABC")));

    }

    @Test
    public void getsOffenderFutureAppointmentsByOffenderId() {
        wireMock.stubFor(
                get(urlPathEqualTo("/offenders/offenderId/12345/appointments"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderAppointments.json"))));



        val appointments = ArrayNode.class.cast(offenderApi.getOffenderFutureAppointmentsByOffenderId("ABC", "12345").toCompletableFuture().join());

        assertThat(appointments.size()).isEqualTo(1);
        wireMock.verify(
                getRequestedFor(
                        urlPathEqualTo("/offenders/offenderId/12345/appointments"))
                        .withQueryParam("from", new EqualToPattern(today()))
                        .withQueryParam("attended", new EqualToPattern("NOT_RECORDED"))
                        .withHeader("Authorization", new EqualToPattern("Bearer ABC")));

    }

    @Test
    public void getsOffenderPersonalCircumstancesByOffenderId() {
        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/personalCircumstances"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderPersonalCircumstances.json"))));


        val personalCircumstances = ArrayNode.class.cast(offenderApi.getOffenderPersonalCircumstancesByOffenderId("ABC", "12345").toCompletableFuture().join());

        assertThat(personalCircumstances.size()).isEqualTo(9);
        wireMock.verify(
                getRequestedFor(
                        urlEqualTo("/offenders/offenderId/12345/personalCircumstances"))
                        .withHeader("Authorization", new EqualToPattern("Bearer ABC")));

    }

    @Test
    public void getsOffenderAccessLimitationsByOffenderId() {
        /*
        {
          "userRestricted": true,
          "restrictionMessage": "This is a restricted offender record",
          "userExcluded": true,
          "exclusionMessage": "This is an excluded offender record"
        }
         */
        wireMock.stubFor(
                get(urlEqualTo("/offenders/offenderId/12345/userAccess"))
                        .willReturn(
                                forbidden()
                                        .withHeader(CONTENT_TYPE, "application/json")
                                        .withBody(loadResource("/deliusoffender/offenderCanAccess.json"))));


        val accessLimitation = offenderApi.checkAccessLimitation("ABC", 12345L).toCompletableFuture().join();

        assertThat(accessLimitation.isUserRestricted()).isTrue();
        assertThat(accessLimitation.getRestrictionMessage()).isEqualTo("This is a restricted offender record");
        assertThat(accessLimitation.isUserExcluded()).isTrue();
        assertThat(accessLimitation.getExclusionMessage()).isEqualTo("This is an excluded offender record");
    }



    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
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
