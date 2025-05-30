package controllers.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import data.base.ReportGeneratorWizardData;
import interfaces.DocumentStore;
import interfaces.OffenderApi;
import interfaces.PdfGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.twirl.api.Content;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReportGeneratorWizardControllerTest_logging {
    @Mock
    HttpExecutionContext ec;
    @Mock
    WebJarsUtil webJarsUtil;
    @Mock
    Config configuration;
    @Mock
    Environment environment;
    @Mock
    MessagesApi messagesApi;
    @Mock
    EncryptedFormFactory formFactory;
    @Mock
    PdfGenerator pdfGenerator;
    @Mock
    DocumentStore documentStore;
    @Mock
    OffenderApi offenderApi;
    private ReportGeneratorWizardController controller;

    @Before
    public void before() {
        controller = new TestController(ec, webJarsUtil, configuration, environment, messagesApi, formFactory, TestData.class, pdfGenerator, documentStore, offenderApi);
    }

    @Test
    public void onlyLogsKeyInformation() {
        assertThat(controller.asLoggableLine(ImmutableMap.<String, String>builder()
                .put("issueBehaviourDetails", "")
                .put("oasysAssessmentsInformationSource", "false")
                .put("pageNumber", "2")
                .put("otherOffences", "Dishonestly retaining a wrongful credit (05332) - 01/08/2018<br>Dishonest representation for obtaining benefit etc (05333)")
                .put("pnc", "2018/123456N")
                .put("court", "Old Bailey")
                .put("mainOffence", "Obtaining a money transfer by deception (05331) - 03/09/2018")
                .put("name", "Sam Henry Jones")
                .put("startDate", "12/03/2019")
                .put("onBehalfOfUser", "johnsmith")
                .put("reportFilename", "shortFormatPreSentenceReport_12032019_151412_JONES_S_B56789.pdf")
                .put("dateOfHearing", "09/08/2018")
                .put("crn", "B56789")
                .put("address", "Main address Building\n" +
                        "7 High Street\n" +
                        "Nether Edge\n" +
                        "Sheffield")
                .put("localJusticeArea", "City of Westminster")
                .put("dateOfBirth", "22/06/2000")
                .put("entityId", "41")
                .put("visitedPages", "[1]")
                .put("documentId", "5c87ccc51a3f8c15525d3da4")
                .put("age", "18")
                .put("lastUpdated", "2019-03-12T15:14:13Z")
                .build()))
                .isEqualTo("{crn=B56789, documentId=5c87ccc51a3f8c15525d3da4, entityId=41, lastUpdated=2019-03-12T15:14:13Z, name=Sam Henry Jones, pageNumber=2, reportFilename=shortFormatPreSentenceReport_12032019_151412_JONES_S_B56789.pdf, visitedPages=[1]}");

    }

    class TestController extends ReportGeneratorWizardController<TestData> {

        TestController(HttpExecutionContext ec, WebJarsUtil webJarsUtil, Config configuration, Environment environment, MessagesApi messagesApi, EncryptedFormFactory formFactory, Class<TestData> wizardType, PdfGenerator pdfGenerator, DocumentStore documentStore, OffenderApi offenderApi) {
            super(ec, webJarsUtil, configuration, environment, messagesApi, formFactory, wizardType, pdfGenerator, documentStore, offenderApi);
        }

        @Override
        protected Map<String, String> storeOffenderDetails(Map params, OffenderApi.Offender offender) {
            return null;
        }

        @Override
        protected String templateName() {
            return null;
        }

        @Override
        protected Content renderCompletedView(Http.Request request, Byte[] bytes) {
            return null;
        }

        @Override
        protected Content renderCancelledView(Http.Request request) {
            return null;
        }

        @Override
        protected String documentEntityType() {
            return null;
        }

        @Override
        protected String documentTableName() {
            return null;
        }

        protected TestData newWizardData() {
            return new TestData();
        }

        protected List<String> paramsToBeLogged() {
            return ImmutableList.<String>builder().addAll(super.paramsToBeLogged()).add("name").build();
        }

    }

    class TestData extends ReportGeneratorWizardData {

    }

}