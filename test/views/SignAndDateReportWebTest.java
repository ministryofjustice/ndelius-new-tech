package views;

import interfaces.AnalyticsStore;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithBrowser;
import utils.SimpleAnalyticsStoreMock;
import utils.SimpleDocumentStoreMock;
import utils.SimplePdfGeneratorMock;
import views.pages.SignAndDateReportPage;
import views.pages.StartPage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static play.inject.Bindings.bind;

public class SignAndDateReportWebTest extends WithBrowser {
    private SignAndDateReportPage signAndDateReportPage;
    private StartPage startPage;

    @Before
    public void before() {
        signAndDateReportPage = new SignAndDateReportPage(browser);
        startPage = new StartPage(browser);
    }

    @Test
    public void shouldBePresentedWithReportAuthorField() {
        assertThat(signAndDateReportPage.navigateHere().hasReportAuthorField()).isTrue();
    }

    @Test
    public void shouldBePresentedWithCounterSignatureField() {
        assertThat(signAndDateReportPage.navigateHere().hasCounterSignatureField()).isTrue();
    }

    @Test
    public void shouldBePresentedWithCourtOfficePhoneNumberField() {
        assertThat(signAndDateReportPage.navigateHere().hasCourtOfficePhoneNumberField()).isTrue();
    }

    @Test
    public void shouldBePresentedWithReadOnlyStartDateField() {
        assertThat(signAndDateReportPage.navigateHere().hasStartDateField()).isTrue();
        assertThat(signAndDateReportPage.navigateHere().isStartDateFieldReadonly()).isTrue();
    }

    @Test
    public void shouldBePresentedWithReadOnlyStartDateFieldUsingTodaysDateForNewReport() {
        assertThat(signAndDateReportPage.navigateHere().getStartDate()).isEqualTo(todaysDate());
    }

    @Test
    public void shouldBePresentedWithReadOnlyStartDateFieldUsingReportDateForExisting() {
        startPage.navigateWithExistingReport();
        assertThat(signAndDateReportPage.getStartDate()).isEqualTo("25/12/2017");
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().
            overrides(
                bind(PdfGenerator.class).toInstance(new SimplePdfGeneratorMock()),
                bind(DocumentStore.class).toInstance(new SimpleDocumentStoreMock() {
                    public CompletionStage<String> retrieveOriginalData(String documentId, String onBehalfOfUser) {
                        return CompletableFuture.supplyAsync(() -> "{\"templateName\": \"fooBar\", \"values\": { \"pageNumber\": \"11\", \"name\": \"Smith,John\", \"address\": \"1234\", \"pnc\": \"Retrieved From Store\",  \"startDate\": \"25/12/2017\" } }");
                    }
                }),
                bind(AnalyticsStore.class).toInstance(new SimpleAnalyticsStoreMock())
            )
            .build();
    }

    private String todaysDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }

}
