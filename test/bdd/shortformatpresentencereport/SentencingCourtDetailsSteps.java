package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.SentencingCourtDetailsPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.SENTENCING_COURT_DETAILS;

public class SentencingCourtDetailsSteps {
    @Inject
    private SentencingCourtDetailsPage page;

    @Given("^that the Delius user is on the \"Sentencing court details\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(SENTENCING_COURT_DETAILS.getPageHeader());
    }

    @Given("^Delius User completes the \"Sentencing court details\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.clickButton("Continue");
    }
}
