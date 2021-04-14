package services.stubs;

import com.google.common.io.ByteStreams;
import interfaces.HealthCheckResult;
import interfaces.PrisonerApi;
import interfaces.PrisonerCategoryApi;
import play.Environment;
import play.Logger;
import play.Mode;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StubPrisonerApi implements PrisonerApi, PrisonerCategoryApi {

    public static String NOMS_NUMBER_OF_FEMALE  = "G8020GG";

    public StubPrisonerApi() {
        Logger.info("Running with stub NomisPrisonerAPI");
    }
    @Override
    public CompletionStage<byte[]> getImage(String nomsNumber) {
        return CompletableFuture.completedFuture(loadJsonResourceBytes("/stubdata/stub-offender-image.jpeg"));
    }

    @Override
    public CompletionStage<HealthCheckResult> isHealthy() {
        return CompletableFuture.completedFuture(HealthCheckResult.healthy());
    }

    @Override
    public CompletionStage<Optional<Offender>> getOffenderByNomsNumber(String nomsNumber) {
        if (NOMS_NUMBER_OF_FEMALE.equals(nomsNumber)) {
            return CompletableFuture.completedFuture(Optional.ofNullable(Offender
                    .builder()
                    .firstName("Jane")
                    .surname("Suzi")
                    .dateOfBirth("2000-06-22")
                    .mostRecentPrisonerNumber("4815")
                    .institution(Institution.builder().description("HMP Leeds").build())
                    .build()));
        }
        return CompletableFuture.completedFuture(Optional.ofNullable(Offender
                .builder()
                .firstName("Sam")
                .surname("Jones")
                .dateOfBirth("1980-12-01")
                .mostRecentPrisonerNumber("4815")
                .institution(Institution.builder().description("HMP Leeds").build())
                .build()));
    }

    @Override
    public CompletionStage<Optional<Category>> getOffenderCategoryByNomsNumber(String nomsNumber) {
        if (NOMS_NUMBER_OF_FEMALE.equals(nomsNumber)) {
            return CompletableFuture.completedFuture(Optional.of(Category.builder().code("T").description("Fem Open").build()));
        }
        return CompletableFuture.completedFuture(Optional.of(Category.builder().code("A").description("Cat A").build()));
    }

    private static byte[] loadJsonResourceBytes(String resource) {
        try {
            return ByteStreams.toByteArray(new Environment(Mode.DEV).resourceAsStream(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
