package bdd.wiremock;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableList;
import helpers.JsonHelper;
import helpers.JwtHelperTest;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import play.Environment;
import play.Mode;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static scala.io.Source.fromInputStream;
import static utils.CourtAppearanceHelpers.aCourtReport;
import static utils.CourtAppearanceHelpers.someCourtAppearances;
import static utils.InstitutionalReportHelpers.anInstitutionalReport;
import static utils.OffenceHelpers.someOffences;
import static utils.OffenderHelper.aFemaleOffenderWithNoContactDetails;
import static utils.OffenderHelper.anOffenderWithNoContactDetails;

public class OffenderApiMock {
    @Inject
    @Named("offenderApiWireMock")
    private WireMockServer offenderApiWireMock;

    @Data
    @Builder
    public static class Sentence {
        private String description;
        private int length;
        private String lengthUnit;

    }

    public OffenderApiMock start() {
            offenderApiWireMock.start();
        return this;
    }

    public OffenderApiMock stop() {
        offenderApiWireMock.stop();
        return this;
    }

    public OffenderApiMock stubDefaults() {
        offenderApiWireMock.stubFor(
                post(urlEqualTo("/documentLink")).willReturn(created()));

        offenderApiWireMock.stubFor(
                post(urlEqualTo("/logon")).willReturn(ok().withBody(JwtHelperTest.generateTokenWithProbationAreaCodes(ImmutableList.of("N01")))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/crn/.*/all"))
                        .willReturn(ok().withBody(JsonHelper.stringify(anOffenderWithNoContactDetails()))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/crn/.*/courtReports/.*"))
                        .willReturn(ok().withBody(JsonHelper.stringify(aCourtReport()))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/crn/.*/offences"))
                        .willReturn(ok().withBody(JsonHelper.stringify(someOffences().getItems()))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/crn/.*/courtAppearances"))
                        .willReturn(ok().withBody(JsonHelper.stringify(someCourtAppearances().getItems()))));


        offenderApiWireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/all"))
                    .willReturn(ok().withBody(JsonHelper.stringify(anOffenderWithNoContactDetails()))));

        offenderApiWireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X54321/all"))
                    .willReturn(ok().withBody(JsonHelper.stringify(aFemaleOffenderWithNoContactDetails()))));

        offenderApiWireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X12345/institutionalReports/12345"))
                    .willReturn(ok().withBody(JsonHelper.stringify(anInstitutionalReport()))));

        offenderApiWireMock.stubFor(
                get(urlEqualTo("/offenders/crn/X54321/institutionalReports/54332"))
                    .willReturn(ok().withBody(JsonHelper.stringify(anInstitutionalReport()))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/all"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offender.json"))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/userAccess"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/userAccess.json"))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/registrations"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderRegistrations.json"))));

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/convictions"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderConvictions.json"))));

        offenderApiWireMock.stubFor(
                get(urlPathMatching("/offenders/offenderId/.*/appointments"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderAppointments.json"))));

        offenderApiWireMock.stubFor(
                get(urlPathMatching("/offenders/offenderId/.*/personalCircumstances"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/offenderPersonalCircumstances.json"))));

        offenderApiWireMock.stubFor(
                get(urlPathMatching("/probationAreas/code/.*"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/probationAreaByCode_Other.json"))));

        return this;
    }

    public OffenderApiMock stubOffenderWithDetails(Map<String, String> offenderDetailsMap) {
        val offender = ObjectNode.class.cast(Json.parse(loadResource("/deliusoffender/offender.json")));

        offenderDetailsMap.forEach((key, value) -> {
            val keysSplit = Arrays.asList(key.split("\\."));
            val branches = keysSplit.subList(0, keysSplit.size() - 1);
            val leaf = keysSplit.get(keysSplit.size() - 1);
            final ObjectNode[] context = {offender};
            branches.forEach(subKey -> {
                context[0] = ObjectNode.class.cast(context[0].get(subKey));
            });

            if (StringUtils.isBlank(value)) {
                context[0].remove(leaf);
            } else {
                context[0].put(leaf, value);
            }
        });

        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/all"))
                        .willReturn(
                                okForContentType("application/json", Json.stringify(offender))));

        return this;
    }

    public OffenderApiMock stubOffenderWithResource(String resource) {
        offenderApiWireMock.stubFor(
                get(urlMatching("/offenders/offenderId/.*/all"))
                        .willReturn(
                                okForContentType("application/json", loadResource("/deliusoffender/" + resource))));


        return this;
    }

    private static String loadResource(String resource) {
        return fromInputStream(new Environment(Mode.TEST).resourceAsStream(resource), "UTF-8").mkString();
    }

}
