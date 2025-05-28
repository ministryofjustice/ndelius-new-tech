package controllers;

import com.typesafe.config.Config;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Result;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ParamsValidatorTest implements ParamsValidator {

    @Mock
    private Config configuration;

    @Mock
    private Runnable errorReporter;

    @Before
    public void setup() {
        when(configuration.getString("params.secret.key")).thenReturn("ThisIsASecretKey");
        when(configuration.getDuration("params.user.token.valid.duration")).thenReturn(Duration.ofHours(1));
    }

    @Test
    public void returns400WhenUsernameIsBlank() {
        val result = invalidCredentials(
            "",
            timePlusMinutesDrift(0),
            errorReporter);

        assertThat(result.get().status()).isEqualTo(400);
    }

    @Test
    public void returns400WhenUsernameIsNull() {
        val result = invalidCredentials(
            null,
            timePlusMinutesDrift(0),
            errorReporter);

        assertThat(result.get().status()).isEqualTo(400);
    }

    @Test
    public void returns400WhenTimeIsBlank() {
        Optional<Result> result = invalidCredentials(
            "john.smith",
            "",
            errorReporter);

        assertThat(result.orElse(new Result(0)).status()).isEqualTo(400);
    }

    @Test
    public void returns400WhenTimeIsNull() {
        Optional<Result> result = invalidCredentials(
            "john.smith",
            null,
            errorReporter);

        assertThat(result.orElse(new Result(0)).status()).isEqualTo(400);
    }

    @Test
    public void returns401WhenTimeDriftIsGreaterThanExpected() {
        Optional<Result> result = invalidCredentials(
            "john.smith",
            timePlusMinutesDrift(61),
            errorReporter);

        assertThat(result.orElse(new Result(0)).status()).isEqualTo(401);
    }

    @Test
    public void returns401WhenTimeDriftIsLessThanExpected() {
        Optional<Result> result = invalidCredentials(
            "john.smith",
            timePlusMinutesDrift(-61),
            errorReporter);

        assertThat(result.orElse(new Result(0)).status()).isEqualTo(401);
    }

    @Test
    public void returnsAnEmptyOptionalWhenCredsAreValid() {
        Optional<Result> result = invalidCredentials(
            "john.smith",
            timePlusMinutesDrift(0),
            errorReporter);

        assertThat(result.isPresent()).isFalse();
    }

    private String timePlusMinutesDrift(int driftInMinutes) {
        return String.valueOf(Instant.now().toEpochMilli() + (1000 * 60 * driftInMinutes));
    }

    @Override
    public Config getConfiguration() {
        return configuration;
    }
}