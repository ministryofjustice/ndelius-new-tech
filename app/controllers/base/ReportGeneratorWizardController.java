package controllers.base;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import data.base.ReportGeneratorWizardData;
import helpers.JsonHelper;
import helpers.ThrowableHelper;
import interfaces.AnalyticsStore;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cglib.beans.BeanMap;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.twirl.api.Content;

import static helpers.FluentHelper.not;
import static helpers.FluentHelper.value;

public abstract class ReportGeneratorWizardController<T extends ReportGeneratorWizardData> extends WizardController<T> {

    private final PdfGenerator pdfGenerator;
    private final DocumentStore documentStore;
    private final boolean standaloneOperation;

    protected ReportGeneratorWizardController(HttpExecutionContext ec,
                                              WebJarsUtil webJarsUtil,
                                              Config configuration,
                                              Environment environment,
                                              AnalyticsStore analyticsStore,
                                              EncryptedFormFactory formFactory,
                                              Class<T> wizardType,
                                              PdfGenerator pdfGenerator,
                                              DocumentStore documentStore) {

        super(ec, webJarsUtil, configuration, environment, analyticsStore, formFactory, wizardType);

        this.pdfGenerator = pdfGenerator;
        this.documentStore = documentStore;

        standaloneOperation = configuration.getBoolean("standalone.operation");
    }

    @Override
    protected CompletionStage<Map<String, String>> initialParams() {

        return super.initialParams().thenCompose(params -> originalData(params).orElseGet(() -> addPageAndDocumentId(params)));
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
    protected final CompletionStage<Result> cancelledWizard(T data) {

        return standaloneOperation ?
                generateReport(data).thenApply(result -> ok(ArrayUtils.toPrimitive(result)).as("application/pdf")) :
                CompletableFuture.supplyAsync(() -> ok(renderCancelledView()), ec.current());
    }

    @Override
    protected final CompletionStage<Result> completedWizard(T data) {

        final Function<Byte[], CompletionStage<Optional<Byte[]>>> resultIfStored = result ->
                storeReport(data, result).thenApply(stored ->
                        Optional.ofNullable(stored.get("ID")).filter(not(Strings::isNullOrEmpty)).map(value(result)));

        final Function<CompletionStage<Byte[]>, CompletionStage<Optional<Byte[]>>> optionalResult = result ->
                standaloneOperation ?
                        result.thenApply(Optional::of) :
                        result.thenCompose(resultIfStored);

        return optionalResult.apply(generateReport(data)).
                exceptionally(error -> {

                    Logger.error("Completed Wizard: Generation or Storage error - " + data.toString(), error);
                    return Optional.empty();
                }).
                thenApplyAsync(result -> result.map(bytes -> ok(renderCompletedView(bytes))).orElseGet(() -> {

                    Logger.warn("Report generator wizard failed");
                    return wizardFailed(data);

                }), ec.current()); // Have to provide execution context for HTTP Context to be available when rendering views
    }

    @Override
    protected final String baseViewName() {

        return "views.html." + templateName() + ".page";
    }

    protected abstract String templateName();

    protected abstract Content renderCompletedView(Byte[] bytes);

    protected abstract Content renderCancelledView();

    private CompletionStage<Map<String, String>> addPageAndDocumentId(Map<String, String> params) {

        params.put("pageNumber", "1");

        return generateAndStoreReport(wizardForm.bind(params).value().orElseGet(this::newWizardData)).
                exceptionally(error -> {

                    Logger.error("Initial Params: Generation or Storage error - " + params.toString(), error);
                    return ImmutableMap.of("errorMessage", ThrowableHelper.toMessageCauseStack(error));
                }).
                thenApply(stored -> {
                    params.put("startDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    params.put("documentId", stored.get("ID"));
                    params.put("errorMessage", stored.get("errorMessage"));

                    if (Strings.isNullOrEmpty(params.get("documentId")) && Strings.isNullOrEmpty(params.get("errorMessage"))) {

                        val errorMessage = stored.get("message");

                        params.put("errorMessage", Strings.isNullOrEmpty(errorMessage) && !standaloneOperation ? "No Document ID" : errorMessage);
                    }

                    return params;
                });
    }

    private Optional<CompletionStage<Map<String, String>>> originalData(Map<String, String> params) {

        return Optional.ofNullable(params.get("documentId")).
                map(documentId -> documentStore.retrieveOriginalData(documentId, params.get("onBehalfOfUser"))).
                map(originalJson -> originalJson.thenApply(json -> JsonHelper.jsonToMap(Json.parse(json).get("values")))).
                map(originalInfo -> originalInfo.thenApply(info -> {

                    info.put("onBehalfOfUser", params.get("onBehalfOfUser"));
                    info.put("documentId", params.get("documentId"));

                    return info;
                }));
    }

    private CompletionStage<Byte[]> generateReport(T data) {

        data.setWatermark(data.getPageNumber() < data.totalPages() ? "DRAFT" : "");

        return pdfGenerator.generate(templateName(), data);
    }

    private CompletionStage<Map<String, String>> storeReport(T data, Byte[] document) {

        val filename = templateName() + ".pdf";
        val metaData = JsonHelper.stringify(ImmutableMap.of(
                "templateName", templateName(),
                "values", BeanMap.create(data)
        ));

        CompletionStage<Map<String, String>> result = null;

        if (Strings.isNullOrEmpty(data.getDocumentId())) {

            result = documentStore.uploadNewPdf(
                    document,
                    filename,
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

            Logger.info("Store result: " + stored);
            return stored;
        });
    }

    private CompletionStage<Map<String, String>> generateAndStoreReport(T data) {

        return generateReport(data).thenCompose(result ->
                standaloneOperation ?
                        CompletableFuture.supplyAsync(ImmutableMap::of) :
                        storeReport(data, result));
    }
}
