package controllers;

import akka.util.ByteString;
import com.mongodb.rx.client.MongoClient;
import data.ShortFormatPreSentenceReportData;
import helpers.Encryption;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import lombok.val;
import org.elasticsearch.client.RestHighLevelClient;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReportGeneratorWizardController_GetPdf_Test extends WithApplication {

    private static final byte[] SOME_PDF_DATA = new byte[]{'p', 'd', 'f'};
    private Function<String, String> encryptor = plainText -> Encryption.encrypt(plainText, "ThisIsASecretKey").orElseThrow(() -> new RuntimeException("Encrypt failed"));

    @Mock
    private DocumentStore alfrescoDocumentStore;
    @Mock
    private PdfGenerator pdfGenerator;

    @Captor
    private ArgumentCaptor<ShortFormatPreSentenceReportData> reportData;

    @Before
    public void beforeEach() {
        when(alfrescoDocumentStore.retrieveDocument(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(SOME_PDF_DATA));

        when(alfrescoDocumentStore.getDocumentName(any(), any()))
                .thenReturn(CompletableFuture.completedFuture("myReport.pdf"));

    }

    @Test
    public void userAndDocumentIdAreDecrypted() {
        val result = route(app, addCSRFToken(getAGetPdfRequest()));

        assertThat(result.status()).isEqualTo(OK);
        verify(alfrescoDocumentStore).retrieveDocument("67890", "pdftester");
    }

    @Test
    public void returnsPdfBytes() {
        val result = route(app, addCSRFToken(getAGetPdfRequest()));

        assertThat(contentAsBytes(result)).isEqualTo(ByteString.fromArray(SOME_PDF_DATA));
    }

    @Test
    public void returnsPdfContentType() {
        val result = route(app, addCSRFToken(getAGetPdfRequest()));

        assertThat(contentType(result)).contains("application/pdf");
    }

    @Test
    public void documentNameUseAsFilename() {
        val result = route(app, addCSRFToken(getAGetPdfRequest()));

        assertThat(result.header(CONTENT_DISPOSITION).get()).isEqualTo("attachment;filename=myReport.pdf;");
    }


    private Http.RequestBuilder getAGetPdfRequest() {
        return new Http.RequestBuilder().
                method(GET).
                uri(String.format("/report/shortFormatPreSentenceReport/get?documentId=%s&onBehalfOfUser=%s",
                        encrypt("67890"),
                        encrypt("pdftester")));
    }

    private String encrypt(String s)  {
        try {
            return URLEncoder.encode(Encryption.encrypt(s, "ThisIsASecretKey").orElseThrow(() -> new RuntimeException("Encrypt failed")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String contentType(Result result) {
        return result.body().contentType().orElseThrow(() -> new AssertionError("Not valid"));
    }

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder().
            overrides(
                bind(PdfGenerator.class).toInstance(pdfGenerator),
                bind(DocumentStore.class).toInstance(alfrescoDocumentStore),
                bind(RestHighLevelClient.class).toInstance(mock(RestHighLevelClient.class)),
                bind(MongoClient.class).toInstance(mock(MongoClient.class))
            )
            .build();
    }
}
