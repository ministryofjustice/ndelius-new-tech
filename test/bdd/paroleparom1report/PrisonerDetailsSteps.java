package bdd.paroleparom1report;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import views.pages.paroleparom1report.PrisonerDetailsPage;

import javax.inject.Inject;

import static views.pages.paroleparom1report.Page.PRISONER_DETAILS;

public class PrisonerDetailsSteps {
    @Inject
    private PrisonerDetailsPage page;

    @Given("^that the Delius user is on the \"Prisoner details\" page within the Parole Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheParoleReport() throws Throwable {
        page.navigateHere();
        page.isAt(PRISONER_DETAILS.getPageHeader());
    }

    @Given("^Delius User completes the \"Prisoner details\" UI within the Parole Report$")
    public void deliusUserCompletesThePageWithinTheParoleReport() throws Throwable {
        page.clickButton("Continue");
    }

    @Given("^that the delius user want to enter for Male prisoner who has Indeterminate sentence$")
    public void thatTheDeliusUserWantToEnterForMalePrisonerWhoHasIndeterminateSentence() throws Throwable {
        // no page action required
    }

    @Given("^that the delius user want to enter for Male prisoner who has Determinate sentence$")
    public void thatTheDeliusUserWantToEnterForMalePrisonerWhoHasDeterminateSentence() throws Throwable {
        // no page action required
    }

    @And("^pause for a bit$")
    public void pauseForABit() throws Throwable {
        Thread.sleep(5000);   }
}
