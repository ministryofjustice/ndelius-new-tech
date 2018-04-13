package views;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import interfaces.AnalyticsStore;
import interfaces.DocumentStore;
import interfaces.PdfGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithBrowser;
import utils.SimpleDocumentStoreMock;
import utils.SimplePdfGeneratorMock;
import views.pages.SearchFeedbackPage;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class SearchFeedbackWebTest extends WithBrowser {
    private SearchFeedbackPage searchFeedbackPage;

    @Mock
    private AnalyticsStore analyticsStore;

    @Before
    public void before() {
        searchFeedbackPage = new SearchFeedbackPage(browser);
    }

    @Test
    public void rendersFeedbackOnPage() {
        when(analyticsStore.nationalSearchFeedback()).thenReturn(CompletableFuture.supplyAsync(() -> ImmutableList.of(feedbackContext())));

        searchFeedbackPage.navigateHere().isAt();

        assertThat(searchFeedbackPage.getSubmittedDate()).isNotEmpty();
        assertThat(searchFeedbackPage.getUsername()).isEqualTo("fake.user");
        assertThat(searchFeedbackPage.getEmailAddress()).isEqualTo("mailto:foo@bar.com");
        assertThat(searchFeedbackPage.getRole()).isEqualTo("Probation Officer");
        assertThat(searchFeedbackPage.getRegion()).isEqualTo("North East");
        assertThat(searchFeedbackPage.getRating()).isEqualTo("Satisfied");
        assertThat(searchFeedbackPage.getProvider()).isEqualTo("NPS");
        assertThat(searchFeedbackPage.getAdditionalComments()).isEqualTo("Some text");
    }

    private ImmutableMap<String, Object> feedbackContext() {
        return ImmutableMap.of(
            "username", "cn=fake.user,cn=Users,dc=moj,dc=com",
            "dateTime", new Date(),
            "feedback", userFeedback());
    }

    private ImmutableMap<String, Object> userFeedback() {
        return ImmutableMap.<String, Object>builder()
            .put("email", "foo@bar.com")
            .put("feedback", "Some text")
            .put("rating", "Satisfied")
            .put("role", "Probation Officer")
            .put("provider", "NPS")
            .put("region", "North East")
            .build();
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().
            overrides(
                bind(PdfGenerator.class).toInstance(new SimplePdfGeneratorMock()),
                bind(DocumentStore.class).toInstance(new SimpleDocumentStoreMock()),
                bind(AnalyticsStore.class).toInstance(analyticsStore)
            ).build();
    }

}
