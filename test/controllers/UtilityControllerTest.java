package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mongodb.rx.client.MongoClient;
import helpers.JsonHelper;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static play.inject.Bindings.bind;
import static play.mvc.Http.RequestBuilder;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UtilityControllerTest extends WithApplication {

    private static int PORT=9101;
    @Rule
    public  WireMockRule wireMock = new WireMockRule(wireMockConfig().port(PORT).jettyStopTimeout(10000L));

    @Before
    public void setup() {
        stubPdfGeneratorWithStatus("OK");
        stubDocumentStoreToReturn(ok("{}"));
        stubOffenderApiToReturn(ok("{}"));
    }

    @Test
    public void healthEndpointIncludesCorrectSections() {

        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);
        val jsonResult = convertToJson(result);

        assertEquals(OK, result.status());
        assertThat(jsonResult).containsOnlyKeys(
            "dateTime",
            "fileSystems",
            "localHost",
            "runtime",
            "version",
            "status",
            "dependencies");
    }

    @Test
    public void healthEndpointIndicatesOkWhenPdfGeneratorIsHealthy() {
        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("pdf-generator", "OK"));
        assertThat(convertToJson(result).get("status")).isEqualTo("OK");
    }

    @Test
    public void healthEndpointIndicatesOkWithDetailWhenPdfGeneratorIsHealthy() throws JsonProcessingException {
        stubPdfGeneratorWithStatus("OK");

        val request = new RequestBuilder().method(GET).uri("/healthcheck?detail=true");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(Json.mapper().readTree(contentAsString(result)).get("dependencies").get("pdf-generator").toPrettyString()).isEqualTo("""
        {
          "healthy" : true,
          "detail" : {
            "status" : "OK"
          }
        }""");
    }

    @Test
    public void healthEndpointIndicatesFailedWhenPdfGeneratorIsUnhealthy() {
        stubPdfGeneratorWithStatus("FAILED");
        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("pdf-generator", "FAILED"));
        assertThat(convertToJson(result).get("status")).isEqualTo("FAILED");
    }

    @Test
    public void healthEndpointIndicatesOkWhenDocumentStoreIsHealthy() {
        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("document-store", "OK"));
        assertThat(convertToJson(result).get("status")).isEqualTo("OK");
    }

    @Test
    public void healthEndpointIndicatesOkWithDetailWhenDocumentStoreIsHealthy() throws JsonProcessingException {
        stubDocumentStoreToReturn(ok("detail is ignored"));

        val request = new RequestBuilder().method(GET).uri("/healthcheck?detail=true");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(Json.mapper().readTree(contentAsString(result)).get("dependencies").get("document-store").toPrettyString()).isEqualTo("""
        {
          "healthy" : true,
          "detail" : "none"
        }""");
    }

    @Test
    public void healthEndpointIndicatesFailedWhenDocumentStoreIsUnhealthy() {
        stubDocumentStoreToReturn(serverError());
        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("document-store", "FAILED"));
        assertThat(convertToJson(result).get("status")).isEqualTo("FAILED");
    }

    @Test
    public void healthEndpointIndicatesOkWhenOffenderApiIsHealthy() {
        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("offender-api", "OK"));
        assertThat(convertToJson(result).get("status")).isEqualTo("OK");
    }

    @Test
    public void healthEndpointIndicatesFailedWhenOffenderApiIsUnhealthy() {
        stubOffenderApiToReturn(serverError());

        val request = new RequestBuilder().method(GET).uri("/healthcheck");

        val result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(dependencies(result)).contains(entry("offender-api", "FAILED"));
        assertThat(convertToJson(result).get("status")).isEqualTo("FAILED");
    }

    private Map<String, Object> convertToJson(Result result) {
        return JsonHelper.jsonToObjectMap(contentAsString(result));
    }

    private void stubPdfGeneratorWithStatus(String status) {
        wireMock.stubFor(
            get(urlEqualTo("/healthcheck"))
                .willReturn(
                    okForContentType("application/json", format("{\"status\": \"%s\"}", status))));
    }

    private void stubDocumentStoreToReturn(ResponseDefinitionBuilder response) {
        wireMock.stubFor(
            get(urlEqualTo("/alfresco/service/noms-spg/notificationStatus"))
                .willReturn(response));
    }

    private void stubOffenderApiToReturn(ResponseDefinitionBuilder response) {
        wireMock.stubFor(
            get(urlEqualTo("/api/health"))
                .willReturn(response));
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("pdf.generator.url", String.format("http://localhost:%d/", PORT))
            .configure("store.alfresco.url", String.format("http://localhost:%d/alfresco/service/", PORT))
            .configure("offender.api.url", String.format("http://localhost:%d/api/", PORT))
            .overrides(
                bind(MongoClient.class).toInstance(mock(MongoClient.class))
            )
            .build();
    }

    private Map<String, String> dependencies(Result result) {
		try {
			val dependencies = Json.mapper().readTree(contentAsString(result)).get("dependencies");
            return Json.mapper().convertValue(dependencies, new TypeReference<>() {});
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    }


}
