package filters;

import helpers.Encryption;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import lombok.val;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.inject.Bindings.bind;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class HttpHeadersTest extends WithApplication {

    @Test
    public void shouldTurnOff_IE_XSSFilter() {
        val request = givenARequestToShortFormatPreSentenceReportAction();

        val result = route(app, addCSRFToken(request));

        assertThat(result.header("X-XSS-Protection").isPresent()).isTrue();
        assertThat(result.header("X-XSS-Protection").get()).isEqualTo("0");
    }

    private Http.RequestBuilder givenARequestToShortFormatPreSentenceReportAction() {
        final Function<String, String> encryptor = plainText -> Encryption.encrypt(plainText, "ThisIsASecretKey").orElseThrow(() -> new RuntimeException("Encrypt failed"));
        val formData = new HashMap<String, String>() {
            {
                put("onBehalfOfUser", encryptor.apply("johnsmith"));
                put("entityId", encryptor.apply("12345"));
                put("documentId", encryptor.apply("67890"));
                put("name", encryptor.apply("John Smith"));
                put("dateOfBirth", encryptor.apply("06/02/1976"));
                put("pageNumber", "2");
                put("jumpNumber", "3");
            }
        };
        return new Http.RequestBuilder().method(POST).bodyForm(formData).uri("/report/shortFormatPreSentenceReport");
    }

    @Override
    protected Application provideApplication() {
        PdfGenerator pdfGenerator = mock(PdfGenerator.class);
        given(pdfGenerator.generate(any(), any())).willReturn(CompletableFuture.supplyAsync(() -> new Byte[0]));

        DocumentStore documentStore = mock(DocumentStore.class);

        return new GuiceApplicationBuilder().
            overrides(
                bind(PdfGenerator.class).toInstance(pdfGenerator),
                bind(DocumentStore.class).toInstance(documentStore)
            )
            .build();
    }
}
