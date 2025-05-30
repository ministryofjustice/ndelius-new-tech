package filters;

import com.google.common.collect.ImmutableMap;
import helpers.JwtHelperTest;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.HashMap;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
@RunWith(MockitoJUnitRunner.Silent.class)
public class RequestLoggerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Http.RequestHeader requestHeader;
    @Mock
    private Result result;

    @Before
    public void setup() {
        when(requestHeader.asScala().session().data()).thenReturn(new HashMap<>());
        when(requestHeader.header(anyString())).thenReturn(Optional.empty());
        when(result.contentType()).thenReturn(Optional.of("text/html"));
    }

    @Test
    public void createsLogLineForPDF() {
        when(result.contentType()).thenReturn(Optional.of("application/pdf"));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.isPresent()).isTrue();
    }
    @Test
    public void createsLogLineForHTML() {
        when(result.contentType()).thenReturn(Optional.of("text/html"));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.isPresent()).isTrue();
    }
    @Test
    public void createsLogLineForJSON() {
        when(result.contentType()).thenReturn(Optional.of("application/json"));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.isPresent()).isTrue();
    }

    @Test
    public void doesNotCreatesLogLineForUnknownContent() {
        when(result.contentType()).thenReturn(Optional.empty());

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.isPresent()).isFalse();
    }

    @Test
    public void doesNotCreatesLogLineForCSS() {
        when(result.contentType()).thenReturn(Optional.of("text/css"));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.isPresent()).isFalse();
    }

    @Test
    public void browserIsLogged() {
        when(requestHeader.header("User-Agent")).thenReturn(Optional.of("Mozilla/5.0 (iPhone; CPU iPhone OS 5_1_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B206 Safari/7534.48.3"));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.get()).contains("'Mobile Safari 5'");
    }

    @Test
    public void unknownBrowserIsLoggedWhenNoUserAgent() {
        when(requestHeader.header("User-Agent")).thenReturn(Optional.empty());

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.get()).contains("'unknown browser'");
    }

    @Test
    public void userIsLoggedWhenPresent() {
        when(requestHeader.asScala().session().data()).thenReturn(
                toScalaImmutableMap(ImmutableMap.of(
                        "offenderApiBearerToken",
                        JwtHelperTest.generateTokenWithSubject("rodger"))));

        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.get()).contains("rodger");
    }

    @Test
    public void unknownUserIsLoggedWhenUserNotPresent() {
        val logLine = RequestLogger.requestLogLine(0, requestHeader, result);

        assertThat(logLine.get()).contains("unknown user");
    }


    private static <K, V> scala.collection.immutable.Map<K, V> toScalaImmutableMap(java.util.Map<K, V> jmap) {
        List<Tuple2<K, V>> tuples = jmap.entrySet()
                .stream()
                .map(e -> Tuple2.apply(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return (Map<K, V>) Map$.MODULE$.apply(JavaConverters.asScalaBuffer(tuples).toSeq());
    }


}