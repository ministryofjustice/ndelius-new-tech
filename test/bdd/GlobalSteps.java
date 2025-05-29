package bdd;

import bdd.wiremock.AlfrescoStoreMock;
import bdd.wiremock.OffenderApiMock;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.val;
import org.assertj.core.api.Assertions;
import play.test.TestBrowser;
import views.pages.ReportPage;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ScenarioScoped
public class GlobalSteps {
    @Inject
    private ReportPage page;

    @Inject
    private TestBrowser control;
    @Inject
    private AlfrescoStoreMock alfrescoStoreMock;
    @Inject
    private OffenderApiMock offenderApiMock;

    private Map<String, String> fieldNameToValues;
    private String whatToIncludeFieldLabel;

    private static int SAVE_THROTTLE_TIME_SECONDS = 5;

    private Date getTestDate(String date) {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        switch (date) {
            case "NEXT_MONTH":
                cal.add(Calendar.MONTH, 1);
                break;
            case "TOMORROW":
                cal.add(Calendar.DATE, 1);
                break;
            case "YESTERDAY":
                cal.add(Calendar.DATE, -1);
                break;
            case "OVER_6_MONTHS_AGO":
                cal.add(Calendar.MONTH, -6);
                cal.add(Calendar.DATE, -2);
                break;
            case "OVER_1_YEAR_AGO":
                cal.add(Calendar.YEAR, -1);
                cal.add(Calendar.DATE, -2);
                break;
        }

        return cal.getTime();
    }

    @Then("^the page should display the following by class name")
    public void pageShouldDisplay(Map<String, String> sectionText) {
        sectionText.forEach((className, text) -> assertThat(page.getPageTextByClassName(className)).isEqualTo(text));
    }


    @And("^the page should not display the following by class name$")
    public void thePageShouldNotDisplayTheFollowingByClassName(List<String> sectionText) {
        sectionText.forEach(className -> assertThat(page.hasSectionWithClassName(className)).isFalse());

    }


    @When("^they select the \"([^\"]*)\" button$")
    public void theySelectTheButton(String button) {
        page.clickButton(button);
    }

    @And("^the user must see the \"([^\"]*)\" button$")
    public void theUserMustSeeTheButton(String buttonName) {
        assertThat(page.verifyButton(buttonName)).isTrue();
    }

    @And("^the user must not see the \"([^\"]*)\" button$")
    public void theUserMustNotSeeTheButton(String buttonName) {
        assertThat(page.verifyButton(buttonName)).isFalse();
    }

    @Then("^the user should be directed to the \"([^\"]*)\" UI$")
    public void the_user_should_be_directed_to_UI(String header) {
        page.isAt(header);
    }

    @When("^they enter the following information$")
    public void theyEnterTheFollowingInformation(Map<String, String> fieldTexts) {
        fieldTexts.forEach((label, text) -> page.fillTextArea(label, Optional.ofNullable(text).orElse("")));
        fieldNameToValues = toNameValues(fieldTexts);
    }

    @When("^they enter the following information into a classic TextArea$")
    public void theyEnterTheFollowingInformationIntoAClassicTextArea(Map<String, String> fieldTexts) {
		fieldTexts.forEach((label, text) -> page.fillClassicTextArea(label, text));
        fieldNameToValues = toNameValues(fieldTexts);
    }

    @When("^they input the following information$")
    public void theyInputTheFollowingInformation(Map<String, String> fieldTexts) {
		fieldTexts.forEach((label, text) -> page.fillInput(label, text));
        fieldNameToValues = toNameValues(fieldTexts);
    }

    @When("^they input the following information based on ID$")
    public void theyInputTheFollowingInformationBasedOnId(Map<String, String> fieldTexts) {
		fieldTexts.forEach((id, text) -> page.fillInputWithId(id, text));
    }

    @When("^they enter the month and year \"([^\"]*)\" for \"([^\"]*)\"$")
    public void theyEnterTheMonthAndYearFor(String dateText, String legend) throws ParseException {
        Date date = !dateText.contains("/") ? getTestDate(dateText) : new SimpleDateFormat("dd/MM/yyyy").parse("1/" + dateText);
        page.fillInputInSectionWithLegend(legend, "Month", new SimpleDateFormat("MM").format(date));
        page.fillInputInSectionWithLegend(legend, "Year", new SimpleDateFormat("yyyy").format(date));
    }

    @When("^they enter the date \"([^\"]*)\" for \"([^\"]*)\"$")
    public void theyEnterTheDateFor(String dateText, String legend) throws ParseException {
        Date date = !dateText.contains("/") ? getTestDate(dateText) : new SimpleDateFormat("dd/MM/yyyy").parse(dateText);

        page.fillInputInSectionWithLegend(legend, "Day", new SimpleDateFormat("dd").format(date));
        page.fillInputInSectionWithLegend(legend, "Month", new SimpleDateFormat("MM").format(date));
        page.fillInputInSectionWithLegend(legend, "Year", new SimpleDateFormat("yyyy").format(date));
    }

    @When("^they remove the stored date for \"([^\"]*)\"$")
    public void theyRemoveTheDateFor(String legend) throws ParseException {
        page.fillInputInSectionWithLegend(legend, "Day", "");
        page.fillInputInSectionWithLegend(legend, "Month", "");
        page.fillInputInSectionWithLegend(legend, "Year", "");
    }

    private Map<String, String> toNameValues(Map<String, String> labelTextMap) {
        return labelTextMap.keySet().stream()
                .map(label -> new SimpleEntry<>(nameFromLabel(label), Optional.ofNullable(labelTextMap.get(label)).orElse("")))
                .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    private String nameFromLabel(String label) {
        return page.fieldNameFromLabel(label);
    }

    @Then("^this information should be saved in the report$")
    public void thisInformationShouldBeSavedInTheReport() {
        control.await().atMost(SAVE_THROTTLE_TIME_SECONDS + 1, TimeUnit.SECONDS).until(unused ->
                alfrescoStoreMock.verifySavedDocumentContainsValues(fieldNameToValues));
    }

    @Then("^the following information should be saved in the report$")
    public void theFollowingInformationShouldBeSavedInTheReport(Map<String, String> fieldNameToValuesTable) throws Throwable {
        control.await().atMost(SAVE_THROTTLE_TIME_SECONDS + 1, TimeUnit.SECONDS).until(unused ->
                alfrescoStoreMock.verifySavedDocumentContainsValues(fieldNameToValuesTable));
    }

    @Given("^the user does not any enter any characters in the free text fields on the page$")
    public void theUserDoesNotAnyEnterAnyCharactersInTheFreeTextFieldsOnThePage() {
        // no page action required
    }

    @Given("^that the user does not select an option on the page$")
    public void thatTheUserDoesNotSelectAnOptionOnThePage() {
        // no page action required
    }

    @Given("^that the user enters no information on the page$")
    public void thatTheUserEntersNoInformationOnThePage() {
        // no page action required
    }

    @When("^they don't enter text into the \"([^\"]*)\"$")
    public void theyDonTEnterTextIntoThe(String label) {
        // no page action required
    }

    @Then("^the following error messages are displayed$")
    public void theFollowingErrorMessagesAreDisplayed(Map<String, String> errorFieldMessages) {
        val nameErrorMessages = toNameValues(errorFieldMessages);
        nameErrorMessages.forEach((name, message) -> assertThat(page.errorMessage(name)).isEqualTo(nameErrorMessages.get(name)));
    }

    @Then("^the following error messages for each field are displayed$")
    public void theFollowingErrorMessagesForEachFieldAreDisplayed(Map<String, String> errorFieldMessages) {
        val nameErrorMessages = errorFieldMessages;
        nameErrorMessages.forEach((name, message) -> assertThat(page.errorMessage(name)).isEqualTo(nameErrorMessages.get(name)));
    }

    @When("^they select the radio button with id \"([^\"]*)\"$")
    public void theySelectTheOptionWithTheId(String id) {
        page.clickElementWithId(id);
    }


    @When("^they select the \"([^\"]*)\" option on the \"([^\"]*)\"$")
    public void theySelectTheOptionOnThe(String label, String legend) {
        page.clickRadioButtonWithLabelWithinLegend(label, legend);
    }

    @Given("^that the \"([^\"]*)\" is ticked$")
    public void thatTheIsTicked(String checkboxLabel) {
        page.clickCheckboxWithLabel(checkboxLabel);
    }

    @Given("^that the Delius user is unclear to what information they need to add to the \"([^\"]*)\" free text field$")
    public void thatTheDeliusUserIsUnclearToWhatInformationTheyNeedToAddToTheFreeTextField(String label) {
        this.whatToIncludeFieldLabel = label;
    }

    @When("^they select \"([^\"]*)\" hyperlink$")
    public void theySelectHyperlink(String text) {
        page.clickSpanWithSiblingLabel(text, whatToIncludeFieldLabel);
    }

    @Given("^that the Delius user is unclear to what information they need to include for the \"([^\"]*)\" radio group$")
    public void thatTheDeliusUserIsUnclearToWhatInformationTheyNeedToAddToTheRadioGroup(String label) {
        this.whatToIncludeFieldLabel = label;
    }

    @When("^they select \"([^\"]*)\" hyperlink within the radio group$")
    public void theySelectHyperlinkWithinRadioGroup(String text) {
        page.clickSpanWithSiblingLegend(text, whatToIncludeFieldLabel);
    }

    @Then("^the UI should expand to show additional content to the end user$")
    public void theUIShouldExpandToShowAdditionalContentToTheEndUser() {
        assertThat(page.whatToIncludeContentVisibleWithSiblingLabel(whatToIncludeFieldLabel)).isTrue();
    }

    @Then("^the UI should expand to show additional content within a radio group to the end user$")
    public void theUIShouldExpandToShowAdditionalContentWithinRadioGroupToTheEndUser() {
        assertThat(page.whatToIncludeContentVisibleWithSiblingLegend(whatToIncludeFieldLabel)).isTrue();
    }

    @When("^they select \"([^\"]*)\" hyperlink from the UI$")
    public void theySelectHyperlinkFromTheUI(String linkText) {
        page.clickLink(linkText);
    }

    @When("^they are select \"([^\"]*)\" link on the navigation menu$")
    public void theyAreSelectLinkOnTheNavigationMenu(String linkText) {
        page.clickLink(linkText);
    }

    @Then("^they must be directed to \"([^\"]*)\" UI$")
    public void theyMustBeDirectedToUI(String pageTitle) {
        page.isAt(pageTitle);
    }

    @Then("^the button for \"([^\"]*)\" must display \"([^\"]*)\"$")
    public void theButtonForMustDisplay(String pageName, String buttonText) {
        Assertions.assertThat(page.statusTextForPage(pageName)).isEqualTo(buttonText);
    }

    @Given("^Delius User closes the Report")
    public void deliusUserCompletesThePageWithinTheReport() {
        page.clickButton("Close");
    }

    @Given("^that the offender has the following offender details in Delius$")
    public void thatTheOffenderHasTheFollowingOffenderDetailsInDelius(Map<String, String> data) {
		offenderApiMock.stubOffenderWithDetails(data);
    }

    @Given("^that the offender has the following data from json file \"([^\"]*)\" in Delius$")
    public void thatTheOffenderHasTheFollowingDataInDelius(String fileName) {
        offenderApiMock.stubOffenderWithResource(fileName + ".json");
    }

    @When("^they expand the \"([^\"]*)\" content section$")
    public void theySelectLinkToExpandSection(String detailsText) {
        page.clickSpanWithClass(detailsText, "govuk-details__summary");
    }

    @When("^they expand the \"([^\"]*)\" accordion$")
    public void theyExpandAccordion(String detailsText) {
        page.clickAccordionWithLabel(detailsText);
    }
}