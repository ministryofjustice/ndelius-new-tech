package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.SourcesOfInformationPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.SOURCES_OF_INFORMATION;

public class SourcesOfInformationSteps {
    @Inject
    private SourcesOfInformationPage page;

    @Given("^that the Delius user is on the \"Sources of information\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(SOURCES_OF_INFORMATION.getPageHeader());
    }

    @Given("^Delius User completes the \"Sources of information\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.clickButton("Continue");
    }
}
