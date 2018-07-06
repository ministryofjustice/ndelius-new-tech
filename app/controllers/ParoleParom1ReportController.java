package controllers;

import com.typesafe.config.Config;
import controllers.base.EncryptedFormFactory;
import controllers.base.ReportGeneratorWizardController;
import data.ParoleParom1ReportData;
import interfaces.AnalyticsStore;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import lombok.val;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.data.Form;
import play.libs.concurrent.HttpExecutionContext;
import play.twirl.api.Content;

import javax.inject.Inject;

public class ParoleParom1ReportController extends ReportGeneratorWizardController<ParoleParom1ReportData> {

    private final views.html.paroleParom1Report.cancelled cancelledTemplate;
    private final views.html.paroleParom1Report.completed completedTemplate;

    @Inject
    public ParoleParom1ReportController(HttpExecutionContext ec,
                                        WebJarsUtil webJarsUtil,
                                        Config configuration,
                                        Environment environment,
                                        AnalyticsStore analyticsStore,
                                        EncryptedFormFactory formFactory,
                                        PdfGenerator pdfGenerator,
                                        DocumentStore documentStore,
                                        views.html.paroleParom1Report.cancelled cancelledTemplate,
                                        views.html.paroleParom1Report.completed completedTemplate,
                                        ParamsValidator paramsValidator) {

        super(ec, webJarsUtil, configuration, environment, analyticsStore, formFactory, ParoleParom1ReportData.class, pdfGenerator, documentStore, paramsValidator);
        this.cancelledTemplate = cancelledTemplate;
        this.completedTemplate = completedTemplate;
    }

    @Override
    protected String templateName() {

        return "paroleParom1Report";
    }

    @Override
    protected Content renderCancelledView() {

        val boundForm = wizardForm.bindFromRequest();

        return cancelledTemplate.render(boundForm, viewEncrypter, reviewPageNumberFor(boundForm));
    }

    @Override
    protected Content renderCompletedView(Byte[] bytes) {

        val boundForm = wizardForm.bindFromRequest();

        return completedTemplate.render(boundForm, viewEncrypter, reviewPageNumberFor(boundForm));
    }

    private Integer reviewPageNumberFor(Form<ParoleParom1ReportData> boundForm) {
        return boundForm.value().map(form -> form.totalPages() - 1).orElse(1);
    }

}
