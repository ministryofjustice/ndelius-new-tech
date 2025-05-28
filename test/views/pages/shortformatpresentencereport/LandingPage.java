package views.pages.shortformatpresentencereport;

import helpers.Encryption;
import io.fluentlenium.core.FluentPage;
import lombok.val;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import play.test.TestBrowser;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LandingPage extends FluentPage {
    @Inject
    public LandingPage(TestBrowser control) {
        super(control);
    }

    public LandingPage navigateHere() {
        goTo("/report/shortFormatPreSentenceReport?onBehalfOfUser=92Q036CvVIRT%2Fi428X3zpg%3D%3D&name=xylkFTVA6GXA1GRZZxZ4MA%3D%3D&crn=v5LH8B7tJKI7fEc9uM76SQ%3D%3D&entityId=RRioaTyIHLGnja2CBw8hqg%3D%3D&user=lJqZBRO%2F1B0XeiD2PhQtJg%3D%3D&t=T2DufYh%2B%2F%2F64Ub6iNtHDGg%3D%3D");
        return this;
    }

    public LandingPage navigateWithExistingReport() {
        val secretKey = "ThisIsASecretKey";
        val clearDocumentId = "12345";
        val clearUserName = "Smith,John";

        try {
            val documentId = URLEncoder.encode(Encryption.encrypt(clearDocumentId, secretKey).orElseThrow(() -> new RuntimeException("Encrypt failed")), "UTF-8");
            val onBehalfOfUser = URLEncoder.encode(Encryption.encrypt(clearUserName, secretKey).orElseThrow(() -> new RuntimeException("Encrypt failed")), "UTF-8");

            goTo("/report/shortFormatPreSentenceReport?documentId=" + documentId +
                    "&onBehalfOfUser=" + onBehalfOfUser +
                    "&user=lJqZBRO%2F1B0XeiD2PhQtJg%3D%3D&t=T2DufYh%2B%2F%2F64Ub6iNtHDGg%3D%3D");
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public LandingPage clickButton(String button) {
        $(By.xpath(String.format("//button[contains(text(),'%s')]", button))).click();
        window().switchTo("reportpopup");
        getDriver().manage().window().setSize(new Dimension(830, 3000));

        return this;
    }

    public LandingPage next() {
        return clickButton("Start now");
    }
}
