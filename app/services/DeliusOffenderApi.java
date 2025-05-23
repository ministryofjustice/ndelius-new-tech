package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import interfaces.HealthCheckResult;
import interfaces.OffenderApi;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import play.Logger;
import play.cache.AsyncCacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.IntFunction;

import static helpers.JsonHelper.readValue;
import static interfaces.HealthCheckResult.healthy;
import static interfaces.HealthCheckResult.unhealthy;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static play.mvc.Http.HeaderNames.AUTHORIZATION;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;

public class DeliusOffenderApi implements OffenderApi {

    private final WSClient wsClient;
    private final String ldapStringFormat;
    private final String offenderApiBaseUrl;
    private final AsyncCacheApi cache;
    private final int cacheTime;

    @Data
    @NoArgsConstructor
    private static class ProbationArea {
        private String code;
        private String description;
    }

    private static TypeReference<List<ProbationArea>> probationAreaListRef = new TypeReference<List<ProbationArea>>(){};

    @Inject
    public DeliusOffenderApi(Config configuration, WSClient wsClient, AsyncCacheApi cache) {
        this.wsClient = wsClient;

        ldapStringFormat = configuration.getString("ldap.string.format");
        offenderApiBaseUrl = configuration.getString("offender.api.url");
        cacheTime = configuration.getInt("offender.api.probationAreas.cache.time.seconds");
        this.cache = cache;
    }

    @Override
    public CompletionStage<String> logon(String username) {
        return wsClient.url(offenderApiBaseUrl + "logon")
            .post(username.equals("NationalUser") ? username : format(ldapStringFormat, username))
            .thenApply(response ->  assertOkResponse(response, "logon"))
            .thenApply(WSResponse::getBody);
    }

    @Override
    public CompletionStage<Boolean> canAccess(String bearerToken, long offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%d/userAccess", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(WSResponse::getStatus)
                .thenApply(status -> {
                    switch(status) {
                        case OK: return true;
                        case FORBIDDEN: return false;
                        default:
                            Logger.error("Got a bad response from {} status {}", url, status);
                            return false;
                    }
                })
                .exceptionally(e -> {
                    Logger.error("Got an error from {}", url, e);
                    return false;
                });

    }

    @Override
    public CompletionStage<AccessLimitation> checkAccessLimitation(String bearerToken, long offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%d/userAccess", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertResponseIn(response, "checkAccessLimitation", ImmutableList.of(OK, FORBIDDEN)))
                .thenApply(WSResponse::getBody)
                .thenApply(body -> readValue(body, AccessLimitation.class));
    }


    @Override
    public CompletionStage<HealthCheckResult> isHealthy() {
        String url = offenderApiBaseUrl + "health";
        return wsClient.url(url)
            .get()
            .thenApply(wsResponse -> {
                if (wsResponse.getStatus() != OK) {
                    Logger.warn("Bad response calling Delius Offender API {}. Status {}", url, wsResponse.getStatus());
                    return unhealthy(String.format("Status: %d", wsResponse.getStatus()));
                }
                return healthy(wsResponse.asJson());
            })
            .exceptionally(throwable -> {
                Logger.error("Got an error calling Delius Offender API health endpoint", throwable);
                return unhealthy(throwable.getLocalizedMessage());
            });
    }

    @Override
    public CompletionStage<JsonNode> searchDb(Map<String, String> queryParams) {

        return logonAndCallOffenderApi("users", queryParams);
    }

    @Override
    public CompletionStage<JsonNode> searchLdap(Map<String, String> queryParams) {

        return logonAndCallOffenderApi("ldap", queryParams);
    }

    @Override
    public CompletionStage<Map<String, String>> probationAreaDescriptions(String bearerToken, List<String> codes) {

        CompletableFuture<Entry<String, String>> futureAreas[] = codes.stream()
                .map(probationAreaCode -> cache.getOrElseUpdate(probationAreaCode, () -> lookupDescription(bearerToken, probationAreaCode), cacheTime))
                .map(CompletionStage::toCompletableFuture)
                .toArray(toCompletableFutures());


        return CompletableFuture.allOf(futureAreas)
                .thenApply(ignoredVoid ->
                        Arrays
                .stream(futureAreas)
                .map(CompletableFuture::join)
                            .collect(toMap(Entry::getKey, Entry::getValue)));

    }

    @Override
    public CompletionStage<Offender> getOffenderByCrn(String bearerToken, String crn) {
        val url = String.format(offenderApiBaseUrl + "offenders/crn/%s/all", crn);
        return wsClient.url(url)
            .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
            .get()
            .thenApply(response -> assertOkResponse(response, "getOffenderByCrn"))
            .thenApply(WSResponse::getBody)
            .thenApply(body -> readValue(body, Offender.class));
    }

    @Override
    public CompletionStage<JsonNode> getOffenderDetailByOffenderId(String bearerToken, String offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%s/all", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertOkResponse(response, "getOffenderDetailByOffenderId"))
                .thenApply(WSResponse::getBody)
                .thenApply(Json::parse);
    }

    @Override
    public CompletionStage<JsonNode> getOffenderRegistrationsByOffenderId(String bearerToken, String offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%s/registrations", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertOkResponse(response, "getOffenderRegistrationsByOffenderId"))
                .thenApply(WSResponse::getBody)
                .thenApply(Json::parse);
    }

    @Override
    public CompletionStage<JsonNode> getOffenderConvictionsByOffenderId(String bearerToken, String offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%s/convictions", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertOkResponse(response, "getOffenderConvictionsByOffenderId"))
                .thenApply(WSResponse::getBody)
                .thenApply(Json::parse);
    }

    @Override
    public CompletionStage<JsonNode> getOffenderFutureAppointmentsByOffenderId(String bearerToken, String offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%s/appointments", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .addQueryParameter("from", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .addQueryParameter("attended", "NOT_RECORDED")
                .get()
                .thenApply(response -> assertOkResponse(response, "getOffenderFutureAppointmentsByOffenderId"))
                .thenApply(WSResponse::getBody)
                .thenApply(Json::parse);
    }

    @Override
    public CompletionStage<JsonNode> getOffenderPersonalCircumstancesByOffenderId(String bearerToken, String offenderId) {
        val url = String.format(offenderApiBaseUrl + "offenders/offenderId/%s/personalCircumstances", offenderId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertOkResponse(response, "getOffenderPersonalCircumstancesByOffenderId"))
                .thenApply(WSResponse::getBody)
                .thenApply(Json::parse);
    }

    @Override
    public CompletionStage<CourtAppearances> getCourtAppearancesByCrn(String bearerToken, String crn) {
        val url = String.format(offenderApiBaseUrl + "offenders/crn/%s/courtAppearances", crn);
        return wsClient.url(url)
            .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
            .get()
            .thenApply(response -> assertOkResponse(response, "getCourtAppearancesByCrn"))
            .thenApply(WSResponse::getBody)
            .thenApply(body -> CourtAppearances.builder()
                .items(readValue(body, new TypeReference<List<CourtAppearance>>() {})).build());
    }

    @Override
    public CompletionStage<CourtReport> getCourtReportByCrnAndCourtReportId(String bearerToken, String crn, String courtReportId) {
        val url = String.format(offenderApiBaseUrl + "offenders/crn/%s/courtReports/%s", crn, courtReportId);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response -> assertOkResponse(response, "getCourtReportByCrnAndCourtReportId"))
                .thenApply(WSResponse::getBody)
                .thenApply(body -> readValue(body, CourtReport.class));
    }

    @Override
    public CompletionStage<Offences> getOffencesByCrn(String bearerToken, String crn) {

        val url = String.format(offenderApiBaseUrl + "offenders/crn/%s/offences", crn);
        return wsClient.url(url)
            .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
            .get()
            .thenApply(response -> assertOkResponse(response, "getOffencesByCrn"))
            .thenApply(WSResponse::getBody)
            .thenApply(body -> Offences.builder()
                .items(readValue(body, new TypeReference<List<Offence>>() {})).build());

    }

    private IntFunction<CompletableFuture<Entry<String, String>>[]> toCompletableFutures() {
        return CompletableFuture[]::new;
    }

    private CompletionStage<Entry<String, String>> lookupDescription(String bearerToken, String probationAreaCode) {
        val url = String.format(offenderApiBaseUrl + "probationAreas/code/%s", probationAreaCode);
        Logger.info("calling " + url);
        return wsClient.url(url)
                .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
                .get()
                .thenApply(response ->  assertOkResponse(response, "probationAreas"))
                .thenApply(WSResponse::getBody)
                .thenApply(body -> {
                    final List<ProbationArea> areas = readValue(body, probationAreaListRef);
                    return areas
                            .stream()
                            .findFirst() // API should return single item in a list
                            .map(area -> new SimpleEntry<>(probationAreaCode, area.getDescription()))
                            .orElse(new SimpleEntry<>(probationAreaCode, probationAreaCode));
                });
    }

    private CompletionStage<JsonNode> logonAndCallOffenderApi(String action, Map<String, String> params) {

        val url = action + queryParamsFrom(params);
        return wsClient.url(offenderApiBaseUrl + "logon")
                .post("NationalUser")
                .thenApply(response ->  assertOkResponse(response, "logon"))
                .thenApply(WSResponse::getBody)
                .thenCompose(bearerToken -> callOffenderApi(bearerToken, url));
    }

    public CompletionStage<JsonNode> callOffenderApi(String bearerToken, String url) {

        return wsClient.url(offenderApiBaseUrl + url)
            .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
            .get()
            .thenApply(wsResponse -> {
                if (wsResponse.getStatus() != OK) {
                    throw new RuntimeException(String.format("Bad response calling Delius Offender API %s. Status %d", url, wsResponse.getStatus()));
                }

                return wsResponse.getContentType().toLowerCase().contains("json") ?
                        wsResponse.asJson() :
                        Json.toJson(ImmutableMap.of(
                                "headers", wsResponse.getHeaders(),
                                "content", Base64.getEncoder().encodeToString(wsResponse.asByteArray()))
                        );

            })
            .exceptionally(throwable -> {
                Logger.error("Got an error calling Delius Offender API", throwable);
                return Json.toJson(ImmutableMap.of("error", throwable.getMessage()));
            });
    }

    @Override
    public CompletionStage<InstitutionalReport> getInstitutionalReport(String bearerToken, String crn, String institutionalReportId) {

        val url = String.format(offenderApiBaseUrl + "offenders/crn/%s/institutionalReports/%s", crn, institutionalReportId);
        return wsClient.url(url)
            .addHeader(AUTHORIZATION, String.format("Bearer %s", bearerToken))
            .get()
            .thenApply(response -> assertOkResponse(response, "getInstitutionalReport"))
            .thenApply(WSResponse::getBody)
            .thenApply(body -> readValue(body, InstitutionalReport.class));
    }

    String queryParamsFrom(Map<String, String> params) {

        return "?" + String.join("&", params.entrySet().stream().
                map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(toList()));
    }

    private WSResponse assertOkResponse(WSResponse response, String description) {
        return assertResponseIn(response, description, ImmutableList.of(OK));
    }
    private WSResponse assertResponseIn(WSResponse response, String description, List<Integer> statuses) {
        if(!statuses.contains(response.getStatus())) {
            Logger.error("{} API bad response {}", description, response.getStatus());
            throw new RuntimeException(String.format("Unable to call %s. Status = %d", description, response.getStatus()));
        }
        return response;
    }
}
