package controllers.base;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import controllers.ParamsValidator;
import data.base.ReportGeneratorWizardData;
import helpers.InvalidCredentialsException;
import interfaces.DocumentStore;
import interfaces.OffenderApi;
import interfaces.OffenderApi.Offender;
import interfaces.PdfGenerator;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanMap;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.Logger;
import play.i18n.Lang;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.typedmap.TypedKey;
import play.libs.typedmap.TypedMap;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static controllers.SessionKeys.OFFENDER_API_BEARER_TOKEN;
import static helpers.FluentHelper.not;
import static helpers.FluentHelper.value;
import static helpers.JsonHelper.*;
import static helpers.JwtHelper.principal;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;

public abstract class ReportGeneratorWizardController<T extends ReportGeneratorWizardData> extends WizardController<T> implements ParamsValidator {

    private final PdfGenerator pdfGenerator;
    private final DocumentStore documentStore;

    protected ReportGeneratorWizardController(HttpExecutionContext ec,
                                              WebJarsUtil webJarsUtil,
                                              Config configuration,
                                              Environment environment,
                                              EncryptedFormFactory formFactory,
                                              Class<T> wizardType,
                                              PdfGenerator pdfGenerator,
                                              DocumentStore documentStore,
                                              OffenderApi offenderApi) {

        super(ec, webJarsUtil, configuration, environment, formFactory, wizardType, offenderApi);

        this.pdfGenerator = pdfGenerator;
        this.documentStore = documentStore;
    }

    @Override
    public Config getConfiguration() {
        return configuration;
    }

    public CompletionStage<Result> reportPost(Http.Request request) {
        return wizardForm.bindFromRequest(request).value().
                map(wizardData -> generateAndStoreReport(wizardData).
                    exceptionally(error -> error(wizardData, error)).
                    thenApply(this::toJsonResult)).
                orElse(CompletableFuture.supplyAsync(() -> badRequestJson(ImmutableMap.of("status", "badRequest")), ec.current()));
    }

    public CompletionStage<Result> getPdf(String documentIdEncrypted, String onBehalfOfUserEncrypted) {
        val onBehalfOfUser = decrypter.apply(onBehalfOfUserEncrypted);
        val documentId = decrypter.apply(documentIdEncrypted);

        return documentStore.retrieveDocument(documentId, onBehalfOfUser).
                thenCombine(
                        documentStore.getDocumentName(documentId, onBehalfOfUser),
                        (bytes, filename) -> ok(bytes).
                                                as("application/pdf").
                                                withHeader(CONTENT_DISPOSITION, String.format("attachment;filename=%s;", filename)));
    }



    private Map<String, String> error(T wizardData, Throwable error) {
        Logger.error("Save: Generation or Storage error - " + wizardData.toString(), error);
        return ImmutableMap.of("errorMessage", error.getMessage());
    }

    private Result toJsonResult(Map<String, String> result) {
        return Optional.ofNullable(result.get("errorMessage")).
                map(data -> serverUnavailableJsonResult(result)).
                orElse(okJson(ImmutableMap.of("status", "ok")));

    }

    private Result serverUnavailableJsonResult(Map<String, String> response) {
        Logger.error("Save: Generation or Storage error - " + response);
        return serverUnavailableJson(ImmutableMap.of("status", "error"));
    }

    @Override
    protected CompletionStage<Map<String, String>> initialParams(Http.Request request) {
        val queryParams = request.queryString().keySet();
        val continueFromInterstitial = queryParams.contains("continue");
        val stopAtInterstitial = queryParams.contains("documentId") && !continueFromInterstitial;

        val user = Optional.ofNullable(request.queryString().get("user"))
            .map(users -> users[0])
            .orElse(null);
        val t = Optional.ofNullable(request.queryString().get("t"))
            .map(times -> times[0])
            .orElse(null);

        final Runnable errorReporter = () -> Logger.error(String.format("Report page request did not receive a valid user (%s) or t (%s)", user, t));

        val possibleBearerTokenRefresh = Optional.of(continueFromInterstitial)
            .filter(not(bool -> bool))
            .map(ignored -> {

                val invalidRequest = invalidCredentials(
                    decrypter.apply(user),
                    decrypter.apply(t),
                    errorReporter);

                if (invalidRequest.isPresent()) {
                    return CompletableFuture.supplyAsync(() -> {
                        throw new InvalidCredentialsException(invalidRequest.get());
                    });
                }

                val username = decrypter.apply(user);

				return offenderApi.logon(username)
					.thenApplyAsync(bearerToken -> {
						Logger.info("AUDIT:{}: ReportGeneratorWizardController: Successful logon for user {}", principal(bearerToken), username);
                        request.addAttr(TypedKey.create(OFFENDER_API_BEARER_TOKEN), bearerToken);
						return bearerToken;
					}, ec.current());
            }).orElse(CompletableFuture.completedFuture("ignored"));

        return possibleBearerTokenRefresh
                .thenCompose(bearerToken -> super.initialParams(request))
                .thenCompose(params ->
            loadExistingDocument(request, params).orElseGet(() -> createNewDocument(request, params))).thenApply(params -> {

            if (stopAtInterstitial) {
                params.put("originalPageNumber", currentPageButNotInterstitialOrCompletion(params.get("pageNumber")));
                params.put("pageNumber", "1");
            }
            if (continueFromInterstitial) {
                params.put("pageNumber", currentPageButNotInterstitialOrCompletion(params.get("pageNumber")));
                params.put("jumpNumber", params.get("pageNumber"));
            }

            return params;
        });
    }

    protected abstract Map<String, String> storeOffenderDetails(Map<String, String> params, Offender offender);
    protected Offender storeReportFilename(Map<String, String> params, Offender offender) {
        params.put(
                "reportFilename",
                String.format("%s_%s_%s_%s_%s.pdf",
                        templateName(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")),
                        offender.getSurname().toUpperCase(),
                        firstLetter(offender.getFirstName()).orElse(""),
                        params.get("crn")));
        return offender;
    }

    private Optional<String> firstLetter(String name) {
        return StringUtils.isBlank(name) ?
                Optional.empty() :
                Optional.of(String.valueOf(name.charAt(0)));
    }

    private String currentPageButNotInterstitialOrCompletion(String pageNumber) {
        // never allow jumping from interstitial  to interstitial, which would happen on
        // saved report that never left the first page or jumping to completion page ("0")
        return String.valueOf(max(parseInt(pageNumber), 2));
    }


    @Override
    protected Integer nextPage(T wizardData) {

        generateAndStoreReport(wizardData).exceptionally(error -> { // Continues in parallel as a non-blocking future result

            Logger.error("Next Page: Generation or Storage error - " + wizardData.toString(), error);
            return ImmutableMap.of();
        });

        return super.nextPage(wizardData);  // Return next page without waiting for draft save to complete
    }

    @Override
    protected final CompletionStage<Result> cancelledWizard(Http.Request request, T data) {

        return CompletableFuture.supplyAsync(() -> ok(renderCancelledView(request)), ec.current());
    }

    @Override
    protected final CompletionStage<Result> completedWizard(Http.Request request, T data) {
        Logger.info("AUDIT:{}: Completed report {} crn={}", data.getOnBehalfOfUser(), templateName(), data.getCrn());

        final Function<Byte[], CompletionStage<Optional<Byte[]>>> resultIfStored = result ->
                storeReport(data, result).thenApply(stored ->
                        Optional.ofNullable(stored.get("ID")).filter(not(Strings::isNullOrEmpty)).map(value(result)));

        final Function<CompletionStage<Byte[]>, CompletionStage<Optional<Byte[]>>> optionalResult = result ->
            result.thenCompose(resultIfStored);

        return optionalResult.apply(generateReport(data)).
                exceptionally(error -> {

                    Logger.error("Completed Wizard: Generation or Storage error - " + data.toString(), error);
                    return Optional.empty();
                }).
                thenApplyAsync(result -> result.map(bytes -> ok(renderCompletedView(request, bytes))).orElseGet(() -> {

                    Logger.warn("Report generator wizard failed");
                    return wizardFailed(request, data);

                }), ec.current()); // Have to provide execution context for HTTP Context to be available when rendering views
    }

    @Override
    protected final String baseViewName() {

        return "views.html." + templateName() + ".page";
    }

    protected abstract String templateName();

    protected abstract Content renderCompletedView(Http.Request request, Byte[] bytes);

    protected abstract Content renderCancelledView(Http.Request request);

    protected CompletionStage<Map<String, String>> createNewDocument(Http.Request request, Map<String, String> params) {

        params.put("pageNumber", "1");
        params.put("startDate", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        val crn = params.get("crn");
        return offenderApi.getOffenderByCrn(getToken(request), crn)
            .thenApply(offender -> storeReportFilename(params, offender))
            .thenApply(offender -> storeOffenderDetails(params, offender))
            .thenCompose(updatedParams -> generateAndStoreReport(wizardForm.bind(Lang.defaultLang(), TypedMap.empty(), updatedParams).value().orElseGet(this::newWizardData)).
                thenApply(stored -> {

                    updatedParams.put("documentId", stored.get("ID"));
                    updatedParams.put("errorMessage", stored.get("errorMessage"));

                    if (Strings.isNullOrEmpty(updatedParams.get("documentId")) && Strings.isNullOrEmpty(updatedParams.get("errorMessage"))) {

                        val errorMessage = stored.get("message");

                        updatedParams.put("errorMessage", Strings.isNullOrEmpty(errorMessage) ? "No Document ID" : errorMessage);
                    }

                    updatedParams.put("createJourney", "true");

                    return updatedParams;
                })
            );
    }

    private Optional<CompletionStage<Map<String, String>>> loadExistingDocument(Http.Request request, Map<String, String> params) {

        return Optional.ofNullable(params.get("documentId")).
            map(documentId -> documentStore.retrieveOriginalData(documentId, params.get("onBehalfOfUser"))).
            map(originalData -> originalData.thenApply(data -> {
                val info = jsonToMap(Json.parse(data.getUserData()).get("values"));
                info.put("lastUpdated", data.getLastModifiedDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                return info;
            })).
            map(originalInfo -> originalInfo.thenComposeAsync(info ->
                offenderApi.getOffenderByCrn(getToken(request), info.get("crn"))
                    .thenApply(offender -> storeOffenderDetails(info, offender)), ec.current())).
            map(originalInfo -> originalInfo.thenApply(info -> {
                info.put("onBehalfOfUser", params.get("onBehalfOfUser"));
                info.put("documentId", params.get("documentId"));
                info.put("user", params.get("user"));
                info.put("t", params.get("t"));

                return info;
            }));
    }

    private CompletionStage<Byte[]> generateReport(T data) {

        data.setWatermark(data.getPageNumber() < data.totalPages() ? "DRAFT" : "");

        return pdfGenerator.generate(templateName(), data);
    }

    private CompletionStage<Map<String, String>> storeReport(T data, Byte[] document) {

        val filename = storedFilename(data);
        val metaData = stringify(ImmutableMap.of(
                "templateName", templateName(),
                "values", convertDataToMap(data)
        ));

        CompletionStage<Map<String, String>> result;

        if (Strings.isNullOrEmpty(data.getDocumentId())) {

            result = documentStore.uploadNewPdf(
                    document,
                    new DocumentStore.DocumentEntity(filename, documentTableName(), documentEntityType()),
                    data.getOnBehalfOfUser(),
                    metaData,
                    data.getCrn(),
                    data.getEntityId());
        } else {

            result = documentStore.updateExistingPdf(
                    document,
                    filename,
                    data.getOnBehalfOfUser(),
                    metaData,
                    data.getDocumentId());
        }

        return result.thenApply(stored -> {

            Logger.debug("Store result: " + stored);
            return stored;
        });
    }

    private String storedFilename(T data) {
        return StringUtils.isBlank(data.getReportFilename()) ?
                templateName() + ".pdf" :
                data.getReportFilename();
    }

    protected abstract String documentEntityType();

    protected abstract String documentTableName();

    protected String asLoggableLine(Map<String, String> params) {
        val keysToKeep = paramsToBeLogged();
        return params
                .keySet()
                .stream()
                .filter(keysToKeep::contains)
                .filter(key -> params.get(key) != null)
                .collect(Collectors.toMap(
                        Function.identity(),
                        params::get,
                        (v1, v2) -> { throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));},
                        TreeMap::new))
                .toString();
    }

    protected List<String> paramsToBeLogged() {
        return ImmutableList.of("pageNumber", "reportFilename", "crn", "entityId", "visitedPages", "documentId", "lastUpdated");
    }


    private Map<String, Object> convertDataToMap(T data) {
        BeanMap beanMap = BeanMap.create(data);

        val dataValues = new HashMap<String, Object>(beanMap);
        List<String> excludedKeys = Arrays.asList("email", "rating", "feedback", "role", "provider", "region");
        excludedKeys.forEach(dataValues::remove);

        return dataValues;
    }

    private CompletionStage<Map<String, String>> generateAndStoreReport(T data) {

        return generateReport(data).thenCompose(result -> storeReport(data, result));
    }

    protected String getToken(Http.Request request) {
        return request.attrs().get(TypedKey.create(OFFENDER_API_BEARER_TOKEN));
    }
}
