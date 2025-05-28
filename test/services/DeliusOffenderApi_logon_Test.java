package services;

import com.typesafe.config.ConfigFactory;
import interfaces.OffenderApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.Silent.class)
public class DeliusOffenderApi_logon_Test {

    private OffenderApi offenderApi;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    @Before
    public void setup() {
        offenderApi = new DeliusOffenderApi(ConfigFactory.load(), wsClient);
        when(wsClient.url(any())).thenReturn(wsRequest);
        when(wsRequest.post(anyString())).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.getStatus()).thenReturn(200);

    }

    @Test
    public void sendsLdapPrincipleToApi() {
        offenderApi.logon("john.smith");

        Mockito.verify(wsRequest).post("cn=john.smith,ou=Users,dc=moj,dc=com");
    }

    @Test
    public void returnsBearerTokenOnSuccessfulLogon() {
        when(wsResponse.getBody()).thenReturn("bearerToken");
        CompletionStage<String> logonResponse = offenderApi.logon("john.smith");

        String token = logonResponse.toCompletableFuture().join();

        assertThat(token).isEqualTo("bearerToken");
    }

    @Test
    public void propagateExceptionOnFailPost() {
        when(wsRequest.post(anyString())).thenReturn(supplyAsync(() -> { throw new RuntimeException("boom"); }));

        CompletionStage<String> logonResponse = offenderApi.logon("john.smith");

        logonResponse
            .thenApply((bearer) -> {
                fail("expected an exception");
                return null;
            })
            .exceptionally((e) -> {
                assertThat(e.getCause().getMessage()).isEqualTo("boom");
                return null;
            })
            .toCompletableFuture().join();
    }
    @Test
    public void propagateExceptionOnHttpResponseError() {
        when(wsResponse.getStatus()).thenReturn(404);

        CompletionStage<String> logonResponse = offenderApi.logon("john.smith");

        logonResponse
            .thenApply((bearer) -> {
                fail("expected an exception");
                return null;
            })
            .exceptionally((e) -> {
                assertThat(e).isInstanceOfAny(RuntimeException.class);
                return null;
            })
            .toCompletableFuture().join();
    }
}