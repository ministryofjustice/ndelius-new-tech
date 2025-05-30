package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.OffenceDetailsPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.OFFENCE_DETAILS;

public class OffenceDetailsSteps {
    @Inject
    private OffenceDetailsPage page;

    @Given("^that the Delius user is on the \"Offence details\" page within the Short Format Pre-sentence Report")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(OFFENCE_DETAILS.getPageHeader());
    }

    @Given("^Delius User completes the \"Offence details\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.fillTextArea("Brief summary of the offence", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nec sem eget lacus euismod vulputate sit amet sed nulla.");
        page.clickButton("Continue");
    }
}
