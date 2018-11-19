package views.pages;

import org.fluentlenium.core.FluentControl;
import org.fluentlenium.core.FluentPage;

import java.util.List;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

public class SourcesOfInformationPage extends FluentPage {
    private final ConclusionPage conclusionPage;
    public SourcesOfInformationPage(FluentControl control) {
        super(control);
        conclusionPage = new ConclusionPage(control);
    }

    public SourcesOfInformationPage navigateHere() {
        conclusionPage.navigateHere().gotoNext();
        return this;
    }

    public SourcesOfInformationPage gotoNext() {
        $(id("nextButton")).click();
        return this;
    }

    public List<String> sourcesOfInformation() {
        return $(cssSelector("label[for*='Source'] span")).texts();
    }

    public boolean isTicked(String informationSourceLabel) {
        final String checkboxId = checkBoxIdFromLabel(informationSourceLabel);
        return $(id(checkboxId)).attribute("checked") != null;    }

    public String otherInformationDetails() {
        return $(id("otherInformationDetails")).text();
    }

    public void attemptNext() {
        $(id("nextButton")).click();
    }

    public SourcesOfInformationPage fillOtherDetailsWith(String details) {
        $(id("otherInformationDetails")).fill().with(details);
        return this;
    }

    public SourcesOfInformationPage tick(String informationSourceLabel) {
        $(xpath(String.format("//label[span[contains(.,'%s')]]", informationSourceLabel))).click();
        return this;
    }

    private String checkBoxIdFromLabel(String optionLabel) {
        return $(xpath(String.format("//label[span[contains(.,'%s')]]", optionLabel))).attribute("for");
    }
}