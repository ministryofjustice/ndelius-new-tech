package views.pages.shortformatpresentencereport;

import org.openqa.selenium.By;
import play.test.TestBrowser;

import javax.inject.Inject;

import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

public class CheckYourReportPage extends ShortFormatPreSentencePopupReportPage {
    private final OffenderDetailsPage offenderDetailsPage;

    @Inject
    public CheckYourReportPage(OffenderDetailsPage offenderDetailsPage, TestBrowser control) {
        super(control);
        this.offenderDetailsPage = offenderDetailsPage;
    }

    public CheckYourReportPage navigateHere() {
        offenderDetailsPage.navigateHere();
        jumpTo(Page.CHECK_YOUR_REPORT);
        return this;
    }

    public CheckYourReportPage gotoNext() {
        $(id("nextButton")).click();
        return this;
    }

    public String statusForOffenderAssessment() {
        return $(xpath("//tr[.//a[text()='Offender assessment']]//strong")).first().text();
    }

    public void clickOffenderDetailsLink() {
        $(By.linkText("Offender details")).click();
    }
}