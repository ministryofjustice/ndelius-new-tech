package bdd.shortformatpresentencereport;

import bdd.wiremock.AlfrescoStoreMock;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.val;
import views.pages.shortformatpresentencereport.LandingPage;
import views.pages.shortformatpresentencereport.OffenderDetailsPage;

import javax.inject.Inject;

import static bdd.wiremock.AlfrescoDocumentBuilder.standardDocument;
import static views.pages.shortformatpresentencereport.Page.OFFENCE_ANALYSIS;

public class LandingPageSteps {
    @Inject
    private LandingPage landingPage;

    @Inject
    private OffenderDetailsPage offenderDetailsPage;

    @Inject
    private AlfrescoStoreMock alfrescoStoreMock;

    @Given("^that the user is on the Short Format Pre-sentence Report landing page$")
    public void thatTheUserIsOnTheLandingPageOfTheReport() {
        landingPage.navigateHere();
    }

    @When("^they select the \"([^\"]*)\" button on the Short Format Pre-sentence Report landing page$")
    public void theySelectTheButton(String button) {
        landingPage.clickButton(button);
    }

    @Given("^that the user is on the Short Format Pre-sentence Report landing page for an existing report$")
    public void thatTheUserIsOnTheParoleReportLandingPageForAnExistingReport() {
        val documentId = "12345";
        alfrescoStoreMock.stubExistingDocument(
                documentId,
                standardDocument().
                        withValuesItem("pageNumber", OFFENCE_ANALYSIS.getPageNumber()).
                        build());
        landingPage.navigateWithExistingReport();
    }

    @Then("^the user should be directed to the last page that they were on working on within the Short Format Pre-sentence Report$")
    public void theUserShouldBeDirectedToTheLastPageThatTheyWereOnWorkingOn() {
        offenderDetailsPage.isAt(OFFENCE_ANALYSIS.getPageHeader());
    }

}