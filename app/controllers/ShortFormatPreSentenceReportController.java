package controllers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import controllers.base.EncryptedFormFactory;
import controllers.base.ReportGeneratorWizardController;
import data.ShortFormatPreSentenceReportData;
import interfaces.DocumentStore;
import interfaces.OffenderApi;
import interfaces.OffenderApi.Court;
import interfaces.OffenderApi.CourtAppearances;
import interfaces.OffenderApi.CourtReport;
import interfaces.OffenderApi.Offences;
import interfaces.OffenderApi.Offender;
import interfaces.PdfGenerator;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.Logger;
import play.data.Form;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.twirl.api.Content;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import static helpers.DateTimeHelper.*;
import static java.time.Clock.systemUTC;
import static java.util.Optional.ofNullable;

public class ShortFormatPreSentenceReportController extends ReportGeneratorWizardController<ShortFormatPreSentenceReportData> {

    private final views.html.shortFormatPreSentenceReport.cancelled cancelledTemplate;
    private final views.html.shortFormatPreSentenceReport.completed completedTemplate;
	private final views.html.helper.error errorTemplate;

	@Inject
    public ShortFormatPreSentenceReportController(HttpExecutionContext ec,
                                                  WebJarsUtil webJarsUtil,
                                                  Config configuration,
                                                  Environment environment,
                                                  MessagesApi messagesApi,
                                                  EncryptedFormFactory formFactory,
                                                  PdfGenerator pdfGenerator,
                                                  DocumentStore documentStore,
                                                  views.html.shortFormatPreSentenceReport.cancelled cancelledTemplate,
                                                  views.html.shortFormatPreSentenceReport.completed completedTemplate,
                                                  views.html.helper.error errorTemplate,
                                                  OffenderApi offenderApi) {

        super(ec, webJarsUtil, configuration, environment, messagesApi, formFactory, ShortFormatPreSentenceReportData.class, pdfGenerator, documentStore, offenderApi);
        this.cancelledTemplate = cancelledTemplate;
        this.completedTemplate = completedTemplate;
		this.errorTemplate = errorTemplate;
	}

    @Override
    protected String templateName() {

        return "shortFormatPreSentenceReport";
    }

    @Override
    protected String documentEntityType() {
        return "COURTREPORT";
    }

    @Override
    protected String documentTableName() {
        return "COURT_REPORT";
    }

    @Override
    protected Map<String, String> storeOffenderDetails(Map<String, String> params, Offender offender) {

        params.put("name", offender.displayName());

        ofNullable(offender.getDateOfBirth()).ifPresent(dob -> {
            params.put("dateOfBirth", format(dob));
            params.put("age", String.format("%d", calculateAge(dob, systemUTC())));
        });

        if (Optional.ofNullable(params.get("pncSupplied"))
                .filter(pncSupplied -> Boolean.TRUE.toString().equals(pncSupplied))
                .isPresent()) {
            params.put("pncSupplied", Boolean.FALSE.toString());
            params.put("pnc", "");
        }
        ofNullable(offender.getOtherIds())
            .filter(otherIds -> otherIds.containsKey("pncNumber"))
            .map(otherIds -> otherIds.get("pncNumber"))
            .ifPresent(pnc -> {
                params.put("pnc", pnc);
                params.put("pncSupplied", Boolean.TRUE.toString());
            });


        if (Optional.ofNullable(params.get("addressSupplied"))
                .filter(addressSupplied -> Boolean.TRUE.toString().equals(addressSupplied))
                .isPresent()) {
            params.put("addressSupplied", Boolean.FALSE.toString());
            params.put("address", "");
        }
        ofNullable(offender.getContactDetails())
            .flatMap(OffenderApi.ContactDetails::mainAddress)
            .map(OffenderApi.OffenderAddress::render)
            .ifPresent(address -> {
                Logger.debug("Using the main address obtained from the API");
                params.put("address", address);
                params.put("addressSupplied", Boolean.TRUE.toString());
            });

        Logger.info("Report params: " + asLoggableLine(params));

        return params;
    }


    @Override
    protected CompletionStage<Map<String, String>> initialParams(Http.Request request) {
        return super.initialParams(request).thenApply(params -> {
            params.putIfAbsent("pncSupplied", Boolean.valueOf(!Strings.isNullOrEmpty(params.get("pnc"))).toString());
            params.putIfAbsent("addressSupplied", Boolean.valueOf(!Strings.isNullOrEmpty(params.get("address"))).toString());
            return migrateLegacyReport(params);
        }).thenComposeAsync(params -> {
            val crn = params.get("crn");
            val token = getToken(params);
            val courtAppearancesFuture = offenderApi.getCourtAppearancesByCrn(token, crn).toCompletableFuture();
            val offencesFuture =  offenderApi.getOffencesByCrn(token, crn).toCompletableFuture();
            val courtReportFuture = offenderApi.getCourtReportByCrnAndCourtReportId(token, crn, params.get("entityId")).toCompletableFuture();

            return CompletableFuture.allOf(courtAppearancesFuture, offencesFuture, courtReportFuture)
                    .thenApplyAsync(notUsed ->
                            storeCourtData(
                                    params,
                                    courtAppearancesFuture.join(),
                                    courtReportFuture.join(),
                                    offencesFuture.join()));
        }, ec.current());
    }

    private Map<String, String> storeCourtData(Map<String, String> params,
                                               CourtAppearances courtAppearances,
                                               CourtReport courtReport,
                                               Offences offences) {

        Logger.debug("CourtAppearances: " + courtAppearances);
        Logger.debug("Offences: " + offences);
        Logger.debug("Params: " + params);
        return Optional.ofNullable(params.get("entityId"))
            .map(Long::parseLong)
            .flatMap(courtAppearances::findForCourtReportId)
            .map(appearance -> {

                if (params.containsKey("createJourney")) {
                    params.put("court", Optional.ofNullable(courtReport.getRequiredByCourt())
                            .map(Court::getCourtName)
                            .orElse(appearance.getCourt().getCourtName()));
                    params.put("mainOffence", offences.mainOffenceDescriptionForId(appearance.mainOffenceId()));
                    params.put("otherOffences", offences.otherOffenceDescriptionsForIds(appearance.otherOffenceIds()));
                    ofNullable(courtReport.getDateRequired())
                            .filter(StringUtils::isNotBlank)
                            .ifPresent(dateOfHearing -> params.put("dateOfHearing", formatDateTime(dateOfHearing)));
                    params.put("localJusticeArea", appearance.getCourt().getLocality());

                }
                return params;
            })
            .orElseGet(() -> {
                        Logger.warn("No court appearance found for given report id");
                        params.put("court", "");
                        return params;
            });
    }


    private Map<String, String> migrateLegacyReport(Map<String, String> params) {
        return migrateLegacyOffenderAssessmentIssues(params);
    }

    private Map<String, String> migrateLegacyOffenderAssessmentIssues(Map<String, String> params) {
        if(Boolean.parseBoolean(params.get("issueDrugs"))) {
            params.putIfAbsent("issueSubstanceMisuse", "true");
        }
        if(Boolean.parseBoolean(params.get("issueAlcohol"))) {
            params.putIfAbsent("issueSubstanceMisuse", "true");
        }
        if(StringUtils.isNotBlank(params.get("offenderAssessment"))) {
            params.putIfAbsent("issueOther", "true");
            params.putIfAbsent("issueOtherDetails", params.get("offenderAssessment"));
        }
        return params;
    }

    @Override
    protected Map<String, String> modifyParams(Map<String, String> params, Consumer<String> paramEncrypter) {

        if ("2".equals(params.get("pageNumber"))) {

            if ("false".equals(params.get("pncSupplied"))) {

                paramEncrypter.accept("pnc");
            }

            if ("false".equals(params.get("addressSupplied"))) {

                paramEncrypter.accept("address");
            }
        }

        if ("3".equals(params.get("pageNumber"))) {
            paramEncrypter.accept("court");
            paramEncrypter.accept("dateOfHearing");
            paramEncrypter.accept("localJusticeArea");
        }

        return params;
    }

    @Override
    protected Content renderCancelledView(Http.Request request) {

        val boundForm = wizardForm.bindFromRequest(request);

        return cancelledTemplate.render(boundForm, viewEncrypter, reviewPageNumberFor(boundForm), request);
    }

    @Override
    protected Content renderCompletedView(Http.Request request, Byte[] bytes) {

        val boundForm = wizardForm.bindFromRequest(request);

        return completedTemplate.render(boundForm, viewEncrypter, reviewPageNumberFor(boundForm), request);
    }

    private Integer reviewPageNumberFor(Form<ShortFormatPreSentenceReportData> boundForm) {
        return boundForm.value().map(form -> form.totalPages() - 1).orElse(1);
    }

    @Override
    protected Content renderErrorMessage(String errorMessage, Http.Request request) {

        return errorTemplate.render("Error - Short Format Pre Sentence Report", errorMessage, request);
    }

    protected List<String> paramsToBeLogged() {
        return ImmutableList.<String>builder().addAll(super.paramsToBeLogged()).add("name").build();
    }

}
