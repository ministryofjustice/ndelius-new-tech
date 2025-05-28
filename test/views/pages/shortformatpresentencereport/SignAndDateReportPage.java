package views.pages.shortformatpresentencereport;

import play.test.TestBrowser;

import javax.inject.Inject;

import static org.openqa.selenium.By.id;

public class SignAndDateReportPage extends ShortFormatPreSentencePopupReportPage {
    private final OffenderDetailsPage offenderDetailsPage;

    @Inject
    public SignAndDateReportPage(OffenderDetailsPage offenderDetailsPage, TestBrowser control) {
        super(control);
        this.offenderDetailsPage = offenderDetailsPage;
    }

    public SignAndDateReportPage navigateHere() {
        offenderDetailsPage.navigateHere();
        jumpTo(Page.SIGNATURE);
        return this;
    }

    public SignAndDateReportPage gotoNext() {
        $(id("reportAuthor")).fill().with("Report Author");
        $(id("office")).fill().with("Office");
        $(id("courtOfficePhoneNumber")).fill().with("0114 555 5555");
        $(id("counterSignature")).fill().with("Counter Signature");
        $(id("nextButton")).click();
        return this;
    }

    public boolean hasCounterSignatureField() {
        return $(id("counterSignature")).present();
    }

    public boolean hasCourtOfficePhoneNumberField() {
        return $(id("courtOfficePhoneNumber")).present();
    }

    public boolean hasStartDateField() {
        return $(id("startDate")).present();
    }

    public boolean isStartDateFieldReadonly() {
        return $(id("startDate")).first().attribute("type").equals("hidden");
    }

    public boolean hasReportAuthorField() {
        return $(id("reportAuthor")).present();
    }

    public String getStartDate() {
        return $(id("value_startDate")).first().text();
    }

    public String getNextButtonText() {
        return $(id("nextButton")).first().text();
    }
}
