package views.pages.shortformatpresentencereport;

import play.test.TestBrowser;

import javax.inject.Inject;
import java.util.List;

import static org.openqa.selenium.By.*;

public class OffenderAssessmentPage extends ShortFormatPreSentencePopupReportPage {
    private final OffenderDetailsPage offenderDetailsPage;

    @Inject
    public OffenderAssessmentPage(OffenderDetailsPage offenderDetailsPage, TestBrowser control) {
        super(control);
        this.offenderDetailsPage = offenderDetailsPage;
    }

    public OffenderAssessmentPage navigateHere() {
        offenderDetailsPage.navigateHere();
        jumpTo(Page.OFFENDER_ASSESSMENT);
        return this;
    }

    public OffenderAssessmentPage gotoNext() {
        clickCheckboxWithLabel("Relationships");
        fillDetailsWith("Relationships", "Relationships details");
        $(id("experienceTrauma_no")).click();
        $(id("caringResponsibilities_no")).click();
        $(id("nextButton")).click();
        return this;
    }


    public OffenderAssessmentPage attemptNext() {
        $(id("nextButton")).click();
        return this;
    }

    public void jumpToPage(String pageNumber) {
        control.executeScript(String.format("$('#jumpNumber').val('%s'); $('form').submit()", pageNumber));
    }


    public List<String> issues() {
        return $(cssSelector("label[for*='issue'] span")).texts();
    }

    public OffenderAssessmentPage fillDetailsWith(String optionLabel, String text) {
        final String checkboxId = checkBoxIdFromLabel(optionLabel);
        final String associatedDetailsId = checkboxId+"Details";
        $(id(associatedDetailsId)).fill().with(text);
        return this;
    }

    public boolean isTicked(String optionLabel) {
        final String checkboxId = checkBoxIdFromLabel(optionLabel);
        return $(id(checkboxId)).first().attribute("checked") != null;
    }

    private String checkBoxIdFromLabel(String optionLabel) {
        return $(xpath(String.format("//label[span[text()='%s']]", optionLabel))).first().attribute("for");
    }

    public String associatedDetailsFor(String optionLabel) {
        final String checkboxId = checkBoxIdFromLabel(optionLabel);
        final String associatedDetailsId = checkboxId+"Details";
        return $(id(associatedDetailsId)).first().text();
    }

    public OffenderAssessmentPage yesWithDetailsFor(String field, String details) {
        $(id(String.format("%s_yes", field))).click();
        $(id(String.format("%sDetails", field))).fill().with(details);
        return this;
    }

    public int countErrors(String errorMessage) {
        return $(xpath(String.format("//span[contains(@class, 'error-message') and text()='%s']", errorMessage))).count();
    }

    public void saveAsDraft() {
        $(id("exitLink")).click();
    }
}
