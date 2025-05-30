package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.OffenceAnalysisPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.OFFENCE_ANALYSIS;

public class OffenceAnalysisSteps {
    @Inject
    private OffenceAnalysisPage page;

    @Given("^that the Delius user is on the \"Offence analysis\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(OFFENCE_ANALYSIS.getPageHeader());
    }

    @Given("^Delius User completes the \"Offence analysis\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.fillTextArea("Offence analysis", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nec sem eget lacus euismod vulputate sit amet sed nulla.");
        page.fillTextArea("Patterns of offending behaviour", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nec sem eget lacus euismod vulputate sit amet sed nulla.");
        page.clickButton("Continue");
    }
}
