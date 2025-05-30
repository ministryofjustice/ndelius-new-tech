package views;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import play.test.Helpers;
import play.test.TestBrowser;

import java.time.Duration;

public class ChromeTestBrowser {
    private static TestBrowser INSTANCE;

    public static TestBrowser create(int port) {
        return provideBrowser(port);
    }

    private static TestBrowser provideBrowser(int port) {
        if (INSTANCE == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.setImplicitWaitTimeout(Duration.ofSeconds(5));
            INSTANCE = Helpers.testBrowser(new ChromeDriver(options), port);
        }
        INSTANCE.getConfiguration().setBaseUrl(String.format("http://localhost:%d", port));
        return INSTANCE;
    }

}
