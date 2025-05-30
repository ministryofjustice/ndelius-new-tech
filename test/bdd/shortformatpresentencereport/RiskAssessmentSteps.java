package bdd.shortformatpresentencereport;

import io.cucumber.java.en.Given;
import views.pages.shortformatpresentencereport.RiskAssessmentPage;

import javax.inject.Inject;

import static views.pages.shortformatpresentencereport.Page.RISK_ASSESSMENT;

public class RiskAssessmentSteps {
    @Inject
    private RiskAssessmentPage page;

    @Given("^that the Delius user is on the \"Risk assessment\" page within the Short Format Pre-sentence Report$")
    public void thatTheDeliusUserIsOnThePageWithinTheReport() throws Throwable {
        page.navigateHere();
        page.isAt(RISK_ASSESSMENT.getPageHeader());
    }

    @Given("^Delius User completes the \"Risk assessment\" UI within the Short Format Pre-sentence Report")
    public void deliusUserCompletesThePageWithinTheReport() throws Throwable {
        page.fillTextArea("Likelihood of further offending", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nec sem eget lacus euismod vulputate sit amet sed nulla.");
        page.fillTextArea("Risk of serious harm", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum nec sem eget lacus euismod vulputate sit amet sed nulla.");
        page.clickRadioButtonWithLabelWithinLegend("Good", "Response to previous supervision");
        page.clickButton("Continue");
    }
}
