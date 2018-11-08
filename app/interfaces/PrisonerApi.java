package interfaces;

import lombok.Builder;
import lombok.Value;

import java.util.concurrent.CompletionStage;

public interface PrisonerApi {

    @Value
    @Builder(toBuilder = true)
    class Institution {
        private String description;

    }

    @Value
    @Builder(toBuilder = true)
    class Offender {
        private Institution institution;

    }

    CompletionStage<byte[]> getImage(String nomsNumber);

    CompletionStage<HealthCheckResult> isHealthy();

    CompletionStage<Offender> getOffenderByNomsNumber(String nomsNumber);
}
