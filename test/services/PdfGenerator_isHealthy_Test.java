package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import interfaces.PdfGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PdfGenerator_isHealthy_Test {

    private PdfGenerator pdfGenerator;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        pdfGenerator = new RestPdfGenerator(ConfigFactory.load(), wsClient);
        when(wsClient.url(any())).thenReturn(wsRequest);
        when(wsRequest.addHeader(any(), any())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
    }

    @Test
    public void returnsHealthyWhenPdfGeneratorReturns200Response() throws IOException {
        when(wsResponse.getStatus()).thenReturn(200);
        when(wsResponse.asJson()).thenReturn(mapper.readTree("{\"status\": \"OK\"}"));

        assertThat(pdfGenerator.isHealthy().toCompletableFuture().join().isHealthy()).isEqualTo(true);
    }

    @Test
    public void returnsUnhealthyWhenPdfGeneratorReturnsNon200Response() {
        when(wsResponse.getStatus()).thenReturn(404);

        assertThat(pdfGenerator.isHealthy().toCompletableFuture().join().isHealthy()).isEqualTo(false);
    }

    @Test
    public void returnsUnHealthyWhenPdfGeneratorCallThrowsException() {
        when(wsRequest.get()).thenReturn(supplyAsync(() -> { throw new RuntimeException("Boom!"); }));

        assertThat(pdfGenerator.isHealthy().toCompletableFuture().join().isHealthy()).isEqualTo(false);
    }

}
