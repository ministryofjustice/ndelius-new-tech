package controllers;

import com.google.common.collect.ImmutableMap;
import com.mongodb.rx.client.MongoClient;
import data.ShortFormatPreSentenceReportData;
import helpers.Encryption;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReportGeneratorWizardController_AutoSave_Test extends WithApplication {

    private static final Byte[] SOME_PDF_DATA = new Byte[]{'p', 'd', 'f'};
    private Function<String, String> encryptor = plainText -> Encryption.encrypt(plainText, "ThisIsASecretKey").orElseThrow(() -> new RuntimeException("Encrypt failed"));

    @Mock
    private DocumentStore alfrescoDocumentStore;
    @Mock
    private PdfGenerator pdfGenerator;

    @Captor
    private ArgumentCaptor<ShortFormatPreSentenceReportData> reportData;

    @Before
    public void beforeEach() {
        when(alfrescoDocumentStore.updateExistingPdf(any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(ImmutableMap.of()));

        when(pdfGenerator.generate(any(), any())).thenReturn(CompletableFuture.completedFuture(SOME_PDF_DATA));
    }

    @Test
    public void autosaveReportSuccessfullyWhenAlfrescoIsWorking() {
        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(result.status()).isEqualTo(OK);
        verify(pdfGenerator).generate(any(), any());
        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), any(), any(), any());
    }

    @Test
    public void autosaveReportReturnsSuccessStatus() {
        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(contentAsString(result)).isEqualTo("{\"status\":\"ok\"}");
    }

    @Test
    public void autosaveReportReturnsJsonContentType() {
        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(contentType(result)).contains("application/json");
    }


    @Test
    public void autosaveReportServerErrorWhenAlfrescoIsNotWorking() {
        when(alfrescoDocumentStore.updateExistingPdf(any(), any(), any(), any(), any()))
            .thenReturn(supplyAsync(() -> { throw new RuntimeException("boom"); }));

        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(contentAsString(result)).isEqualTo("{\"status\":\"error\"}");
    }

    @Test
    public void autosaveReportReturnsErrorWhenAlfrescoIsNotWorking() {
        when(alfrescoDocumentStore.updateExistingPdf(any(), any(), any(), any(), any()))
            .thenReturn(supplyAsync(() -> { throw new RuntimeException("boom"); }));

        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(result.status()).isEqualTo(SERVICE_UNAVAILABLE);
        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), any(), any(), any());
    }

    @Test
    public void autosaveReportReturnsErrorWhenAlfrescoReturnsError() {
        when(alfrescoDocumentStore.updateExistingPdf(any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(ImmutableMap.of("errorMessage", "It has all gone wrong")));

        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(result.status()).isEqualTo(SERVICE_UNAVAILABLE);
        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), any(), any(), any());
    }

    @Test
    public void autosaveReportServerErrorWhenPdfGenerationFails() {
        when(pdfGenerator.generate(any(), any())).thenReturn(supplyAsync(() -> { throw new RuntimeException("boom"); }));

        val result = route(app, addCSRFToken(givenAnAutoSaveRequest()));

        assertThat(result.status()).isEqualTo(SERVICE_UNAVAILABLE);
        verify(alfrescoDocumentStore, never()).updateExistingPdf(any(), any(), any(), any(), any());
    }

    @Test
    public void autosaveReportSavesGeneratedPdf() {
        route(app, addCSRFToken(givenAnAutoSaveRequest()));

        verify(alfrescoDocumentStore).updateExistingPdf(eq(SOME_PDF_DATA), any(), any(), any(), any());
    }

    @Test
    public void autosaveReportSavesGeneratedPdfNameFromTemplateNameController() {
        route(app, addCSRFToken(givenAnAutoSaveRequest()));

        verify(pdfGenerator).generate(eq("shortFormatPreSentenceReport"), any());
        verify(alfrescoDocumentStore).updateExistingPdf(any(), eq("shortFormatPreSentenceReport.pdf"), any(), any(), any());
    }

    @Test
    public void autosaveReportSavesWithUsername() {
        route(app, addCSRFToken(givenAnAutoSaveRequestWithFormDataIncluding("onBehalfOfUser", encryptor.apply("Smith,John"))));

        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), eq("Smith,John"), any(), any());
    }


    @Test
    public void autosaveReportSavesWithDocumentId() {
        route(app, addCSRFToken(givenAnAutoSaveRequestWithFormDataIncluding("documentId", encryptor.apply("9928299"))));

        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), any(), any(), eq("9928299"));
    }


    @Test
    public void autosaveReportSavesWithFormDataAsMetaDataJson() {
        route(app, addCSRFToken(givenAnAutoSaveRequestWithFormDataIncluding("offenceSummary", "Knife attack")));

        verify(alfrescoDocumentStore).updateExistingPdf(any(), any(), any(), contains("\"offenceSummary\":\"Knife attack\""), any());
    }

    @Test
    public void autosaveReportCreatesPdfWithFormData() {
        route(app, addCSRFToken(givenAnAutoSaveRequestWithFormDataIncluding("offenceSummary", "Knife attack")));

        verify(pdfGenerator).generate(any(), reportData.capture());

        assertThat(reportData.getValue().getOffenceSummary()).isEqualTo("Knife attack");
    }


    private Http.RequestBuilder givenAnAutoSaveRequest() {
        val formData = someFormData();
        return new Http.RequestBuilder().method(POST).bodyForm(formData).uri("/report/shortFormatPreSentenceReport/save");
    }

    private String contentType(Result result) {
        return result.body().contentType().orElseThrow(() -> new AssertionError("Not json"));
    }

    private HashMap<String, String> someFormData() {
        return new HashMap<String, String>() {
            {
                put("onBehalfOfUser", encryptor.apply("autosave"));
                put("entityId", encryptor.apply("12345"));
                put("documentId", encryptor.apply("67890"));
                put("name", encryptor.apply("John Smith"));
                put("dateOfBirth", encryptor.apply("06/02/1976"));
                put("pageNumber", "2");
                put("jumpNumber", "3");
            }
        };
    }

    private Http.RequestBuilder givenAnAutoSaveRequestWithFormDataIncluding(String key, String value) {
        val formData = someFormData();
        formData.put(key, value);
        return new Http.RequestBuilder().method(POST).bodyForm(formData).uri("/report/shortFormatPreSentenceReport/save");
    }

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder().
            overrides(
                bind(PdfGenerator.class).toInstance(pdfGenerator),
                bind(DocumentStore.class).toInstance(alfrescoDocumentStore),
                bind(MongoClient.class).toInstance(mock(MongoClient.class))
            )
            .build();
    }
}
