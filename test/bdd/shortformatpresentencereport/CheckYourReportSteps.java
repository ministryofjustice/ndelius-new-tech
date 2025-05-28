package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.CheckYourReportPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.CHECK_YOUR_REPORT;

public class CheckYourReportSteps {
    @Inject
    private CheckYourReportPage page;

    @Given("^that the Delius user is on the \"Check your report\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(CHECK_YOUR_REPORT.getPageHeader());
    }

    @Given("^Delius User is ready to sign their Short Format Pre-sentence Report$")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.clickButton("Sign");
    }
}