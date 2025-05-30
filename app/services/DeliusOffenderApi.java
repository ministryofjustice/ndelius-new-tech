package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import interfaces.HealthCheckResult;
import interfaces.OffenderApi;
import lombok.val;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static helpers.JsonHelper.readValue;
import static interfaces.HealthCheckResult.healthy;
import static interfaces.HealthCheckResult.unhealthy;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static play.mvc.Http.HeaderNames.AUTHORIZATION;
import static play.mvc.Http.Status.OK;

public class DeliusOffenderApi implements OffenderApi {

    private final WSClient wsClient;
    private final String ldapStringFormat;
    private final String offenderApiBaseUrl;

    @Inject
    public DeliusOffenderApi(Config configuration, WSClient wsClient) {
        this.wsClient = wsClient;

        ldapStringFormat = configuration.getString("ldap.string.format");
        offenderApiBaseUrl = configuration.getString("offender.api.url");
    }

    @Override
    public CompletionStage<String> logon(String username) {
        return wsClient.url(offenderApiBaseUrl + "logon")
            .post(username.equals("NationalUser") ? username : format(ldapStringFormat, username))
            .thenApply(response ->  assertOkResponse(response, "logon"))
            .thenApply(WSResponse::getBody);
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
