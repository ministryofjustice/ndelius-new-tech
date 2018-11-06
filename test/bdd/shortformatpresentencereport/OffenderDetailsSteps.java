package bdd.shortformatpresentencereport;

import cucumber.api.java.en.Given;
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

}
