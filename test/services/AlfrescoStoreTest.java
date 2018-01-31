package services;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoStoreTest {

    private AlfrescoStore alfrescoStore;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    @Before
    public void setup() {
        alfrescoStore = new AlfrescoStore(ConfigFactory.load(), wsClient);
        when(wsClient.url(any())).thenReturn(wsRequest);
        when(wsRequest.addHeader(any(), any())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
    }

    @Test
    public void returnsHealthyWhenAlfrescoReturns200Response() {
        when(wsResponse.getStatus()).thenReturn(200);

        assertThat(alfrescoStore.isHealthy().toCompletableFuture().join()).isEqualTo(true);
    }

    @Test
    public void returnsUnhealthyWhenAlfrescoReturnsNon200Response() {
        when(wsResponse.getStatus()).thenReturn(404);

        assertThat(alfrescoStore.isHealthy().toCompletableFuture().join()).isEqualTo(false);
    }

    @Test
    public void returnsUnHealthyWhenAlfrescoCallThrowsException() {
        when(wsRequest.get()).thenReturn(supplyAsync(() -> { throw new RuntimeException("Boom!"); }));

        assertThat(alfrescoStore.isHealthy().toCompletableFuture().join()).isEqualTo(false);
    }

}
