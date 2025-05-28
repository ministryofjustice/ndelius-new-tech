package views.pages;

import io.fluentlenium.core.FluentPage;
import io.fluentlenium.core.domain.FluentWebElement;
import lombok.val;
import org.openqa.selenium.By;
import play.test.TestBrowser;

import javax.inject.Inject;
import java.util.Optional;

import static org.openqa.selenium.By.*;

public class ReportPage extends FluentPage {

    @Inject
    public ReportPage(TestBrowser control) {
        super(control);
    }

    @Override
    public void isAt(Object... parameters) {
        control.await().until(driver -> driver.find(By.tagName("h1")).first().text().equals(parameters[0]));
    }

    public void jumpTo(String pageNumber) {
        String linkSelector = String.format("//a[@data-target='%s']", pageNumber);
        control.await().until(driver -> driver.find(By.xpath(linkSelector)).size() == 1);
        $(By.xpath(linkSelector)).click();
    }

    public boolean verifyButton(String button) {
        return $(By.xpath(String.format("//button[contains(text(),'%s')]", button))).present();
    }

    public void clickButton(String button) {
        $(By.xpath(String.format("//button[contains(text(),'%s')]", button))).click();
    }

    public void fillTextAreaById(String id, String text) {
        control.executeScript(String.format("tinymce.get('%s-tinymce').fire('focus')", id));
        control.executeScript(String.format("tinymce.get('%s-tinymce').setContent('%s')", id, text.replace("'", "\\'")));
        control.executeScript(String.format("tinymce.get('%s-tinymce').fire('keyup')", id));
        control.executeScript(String.format("tinymce.get('%s-tinymce').fire('blur')", id));
    }

    public void fillTextArea(String label, String text) {
        String fieldId = $(xpath(String.format("//label[span[text()='%s']]", label))).first().attribute("for");
        this.fillTextAreaById(fieldId, text);
    }

    public void fillClassicTextArea(String label, String text) {
        val fieldId = $(xpath(String.format("//label[span[text()='%s']]", label))).first().attribute("for");
        $(name(fieldId)).fill().with(text);
    }

    public void fillInput(String label, String text) {
        String fieldId = $(xpath(String.format("//label[span[text()='%s']]", label))).first().attribute("for");
        this.fillInputWithId(fieldId, text);
    }

    public void fillInputWithId(String fieldId, String text) {
        $(id(fieldId)).fill().with(text);
        control.executeScript(String.format("document.getElementById('%s').blur()", fieldId));
    }

    public void fillInputInSectionWithLegend(String legend, String label, String text) {
        String fieldId = findSectionFromLegend(legend).find(xpath(String.format(".//label[contains(., '%s')]", label))).first().attribute("for");
        this.fillInputWithId(fieldId, text);
    }

    public String fieldNameFromLabel(String label) {
        val fieldId = $(xpath(String.format("//label[span[text()='%s']]", label))).first().attribute("for");
        return Optional.ofNullable(fieldId).orElseGet(() -> Optional.ofNullable(dateGroupFieldIdFromLegend(label)).orElseGet(() -> radioFieldNameFromLegend(label)));
    }

    private String radioFieldNameFromLegend(String legend) {
        // find any radio input within section with legend - we just need the name of form field name
        return findSectionFromLegend(legend).find(By.cssSelector("input[type='radio']")).first().attribute("name");
    }

    private String dateGroupFieldIdFromLegend(String legend) {
        // find any radio input within section with legend - we just need the name of form field name
        return findSectionFromLegend(legend).find(By.cssSelector(".govuk-date-input")).first().attribute("id");
    }

    public void clickElementWithId(String id) {
        $(id(id)).first().click();
    }

    public void clickRadioButtonWithLabelWithinLegend(String label, String legend) {
        val fieldId = fieldNameFromLabelWithLegend(label, legend);
        $(id(fieldId)).first().scrollIntoView(true).click();
    }

    private String fieldNameFromLabelWithLegend(String label, String legend) {
        return findSectionFromLegend(legend)
                .find(xpath(String.format(".//label[contains(.,'%s')]", label))).first()
                .attribute("for");
    }

    private FluentWebElement findSectionFromLegend(String legend) {
        return $(xpath(String.format("//fieldset[legend[contains(.,'%s')]]", legend))).last();
    }

    public String errorMessage(String name) {
        return $(xpath(String.format("//a[@href='#%s-error']", name))).first().text();
    }

    public void clickCheckboxWithLabel(String label) {
        val fieldId = $(xpath(String.format("//label[span[text()='%s']]", label))).first().attribute("for");
        $(id(fieldId)).first().click();
    }

    public void clickSpanWithSiblingLabel(String text, String label) {
        val parent = $(xpath(String.format("//label[span[text()='%s']]", label))).first().find(By.xpath("./.."));
        parent.find(xpath(String.format(".//span[contains(.,'%s')]", text))).click();
    }

    public void clickSpanWithSiblingLegend(String text, String label) {
        val parent = $(xpath(String.format("//legend[span[text()='%s']]", label))).first().find(By.xpath("./.."));
        parent.find(xpath(String.format(".//span[contains(.,'%s')]", text))).click();
    }

    public boolean whatToIncludeContentVisibleWithSiblingLabel(String label) {
        val parent = $(xpath(String.format("//label[span[text()='%s']]", label))).first().find(By.xpath("./.."));
        return parent.find(By.cssSelector(".govuk-details__text")).first().displayed();
    }

    public boolean whatToIncludeContentVisibleWithSiblingLegend(String label) {
        val parent = $(xpath(String.format("//legend[span[text()='%s']]", label))).first().find(By.xpath("./.."));
        return parent.find(By.cssSelector(".govuk-details__text")).first().displayed();
    }

    public String statusTextForPage(String pageName) {
        val row = $(By.linkText(pageName)).find(By.xpath("../.."));
        val statusCell = row.find(By.cssSelector("td:nth-child(2)"));
        return statusCell.first().text();
    }

    public String getPageTextByClassName(String className) {
        return $(By.className(className)).first().text();
    }

    public boolean hasSectionWithClassName(String className) {
        return $(By.className(className)).size() > 0;
    }

    public void clickLink(String linkText) {
        $(By.linkText(linkText)).click();
    }

    public void clickSpanWithClass(String text, String className) {
        val parent = $(By.className(className));
        parent.find(xpath(String.format(".//span[contains(.,'%s')]", text))).click();
    }

    public void clickAccordionWithLabel(String text) {
        val parent = $(By.className("govuk-accordion__section-heading"));
        parent.find(xpath(String.format(".//button[contains(.,'%s')]", text))).click();
    }
}
