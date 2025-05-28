package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.OffenderDetailsPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.OFFENDER_DETAILS;

public class OffenderDetailsSteps {
    @Inject
    private OffenderDetailsPage page;

    @Given("^that the Delius user is on the \"Offender details\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(OFFENDER_DETAILS.getPageHeader());
    }

    @Given("^Delius User completes the \"Offender details\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.clickButton("Continue");
    }

}
