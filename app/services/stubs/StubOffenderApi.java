package services.stubs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import interfaces.HealthCheckResult;
import interfaces.OffenderApi;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static interfaces.HealthCheckResult.healthy;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class StubOffenderApi implements OffenderApi {
    public static String NOMS_NUMBER_OF_FEMALE  = "G8020GG";

    @Override
    public CompletionStage<String> logon(String username) {
        // JWT Header/Body is {"alg":"HS512"}{"sub":"cn=fake.user,cn=Users,dc=moj,dc=com","uid":"fake.user","probationAreaCodes":["N02", "N01", "N03", "N04", "C01", "C16"],"exp":1523599298}
        return CompletableFuture.completedFuture("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjbj1mYWtlLnVzZXIsY249VXNlcnMsZGM9bW9qLGRjPWNvbSIsInVpZCI6ImZha2UudXNlciIsInByb2JhdGlvbkFyZWFDb2RlcyI6WyJOMDIiLCAiTjAxIiwgIk4wMyIsICJOMDQiLCAiQzAxIiwgIkMxNiJdLCJleHAiOjE1MjM1OTkyOTh9.FsI0VbLbqLRUGo7GXDEr0hHLvDRJjMQWcuEJCCaevXY1KAyJ_05I8V6wE6UqH7gB1Nq2Y4tY7-GgZN824dEOqQ");
    }

    @Override
    public CompletionStage<HealthCheckResult> isHealthy() {
        return CompletableFuture.completedFuture(healthy());
    }

    @Override
    public CompletionStage<Offender> getOffenderByCrn(String bearerToken, String crn) {
        if (isBlank(bearerToken)) {
            throw new RuntimeException("getOffenderByCrn called with blank bearerToken");
        }

        if (isBlank(crn)) {
            throw new RuntimeException("getOffenderByCrn called with blank CRN");
        }

        if (crn.equals("X54321")) {
            return CompletableFuture.completedFuture(Offender.builder()
                .firstName("Lucy")
                .surname("Jones")
                .middleNames(ImmutableList.of("Jane", "Suzi"))
                .dateOfBirth("1980-12-01")
                .otherIds(otherIds(NOMS_NUMBER_OF_FEMALE))
                .contactDetails(contactDetails())
                .gender("Female")
                .build());
        } else {
            return CompletableFuture.completedFuture(Offender.builder()
                .firstName("Sam")
                .surname("Jones")
                .middleNames(ImmutableList.of("Henry", "James"))
                .dateOfBirth("2000-06-22")
                .otherIds(otherIds("G8678GG"))
                .contactDetails(contactDetails())
                .gender("Male")
                .build());
        }
    }

    @Override
    public CompletionStage<CourtAppearances> getCourtAppearancesByCrn(String bearerToken, String crn) {
        if (isBlank(bearerToken)) {
            throw new RuntimeException("getOffenderByCrn called with blank bearerToken");
        }

        if (isBlank(crn)) {
            throw new RuntimeException("getOffenderByCrn called with blank CRN");
        }

        val courtAppearances = CourtAppearances.builder()
            .items(ImmutableList.of(CourtAppearance.builder()
                .courtAppearanceId(12345L)
                .appearanceDate("2018-08-01T00:00:00")
                .court(Court.builder()
                    .courtName("High Court")
                    .locality("City of Westminster")
                    .build())
                .courtReports(ImmutableList.of(
                    CourtReport.builder()
                        .courtReportId(41L)
                        .build(),
                    CourtReport.builder()
                        .courtReportId(2L)
                        .build()
                    ))
                .offenceIds(ImmutableList.of("M1", "A1", "A2"))
                .build()))
            .build();

        return CompletableFuture.completedFuture(courtAppearances);

    }

    @Override
    public CompletionStage<CourtReport> getCourtReportByCrnAndCourtReportId(String bearerToken, String crn, String courtReportId) {
        return CompletableFuture.completedFuture(CourtReport.builder()
                .courtReportId(Long.valueOf(courtReportId))
                .dateRequired("2018-08-09T00:00:00")
                .requiredByCourt(Court.builder()
                        .courtName("Old Bailey")
                        .locality("City of Westminster")
                        .build())
                .build());
    }

    @Override
    public CompletionStage<Offences> getOffencesByCrn(String bearerToken, String crn) {
        if (isBlank(bearerToken)) {
            throw new RuntimeException("getOffencesByCrn called with blank bearerToken");
        }

        if (isBlank(crn)) {
            throw new RuntimeException("getOffencesByCrn called with blank CRN");
        }

        val offences = Offences.builder()
            .items(ImmutableList.of(
                aMainOffence(),
                additionalOffence1(),
                additionalOffence2())
            ).build();

        return CompletableFuture.completedFuture(offences);
    }

    private Offence additionalOffence1() {
        return Offence.builder()
            .mainOffence(false)
            .offenceId("A1")
            .offenceDate("2018-08-01T00:00:00")
            .detail(OffenceDetail.builder()
                .code("05332")
                .description("Dishonestly retaining a wrongful credit - 05332")
                .mainCategoryCode("053")
                .mainCategoryDescription("Other frauds (Category)")
                .subCategoryCode("32")
                .subCategoryDescription("Dishonestly retaining a wrongful credit")
                .build())
            .build();
    }

    private Offence additionalOffence2() {
        return Offence.builder()
            .mainOffence(false)
            .offenceId("A2")
            .offenceCount(2L)
            .detail(OffenceDetail.builder()
                .code("05333")
                .description("Dishonest representation for obtaining benefit etc - 05333")
                .mainCategoryCode("053")
                .mainCategoryDescription("Other frauds (Category)")
                .subCategoryCode("33")
                .subCategoryDescription("Dishonest representation for obtaining benefit etc")
                .build())
            .build();
    }

    private Offence aMainOffence() {
        return Offence.builder()
            .mainOffence(true)
            .offenceId("M1")
            .offenceDate("2018-09-03T00:00:00")
            .offenceCount(3L)
            .detail(OffenceDetail.builder()
                .code("05331")
                .description("Obtaining a money transfer by deception - 05331")
                .mainCategoryCode("053")
                .mainCategoryDescription("Other frauds (Category)")
                .subCategoryCode("31")
                .subCategoryDescription("Obtaining a money transfer by deception")
                .build())
            .build();
    }

    private ContactDetails contactDetails() {
        return ContactDetails.builder().addresses(addresses()).build();
    }

    private ImmutableMap<String, String> otherIds(String nomisNumber) {
        return ImmutableMap.of("pncNumber", "2018/123456N", "nomsNumber", nomisNumber);
    }

    private ImmutableList<OffenderAddress> addresses() {
        return ImmutableList.of(anAddress(), anOtherAddress());
    }

    private OffenderAddress anAddress() {
        return OffenderAddress.builder()
            .buildingName("Main address Building")
            .addressNumber("7")
            .streetName("High Street")
            .district("Nether Edge")
            .town("Sheffield")
            .county("Yorkshire")
            .postcode("S7 1AB")
            .from("2010-06-22")
            .status(AddressStatus.builder()
                .code("M")
                .description("Main")
                .build())
            .build();
    }

    private OffenderAddress anOtherAddress() {
        return OffenderAddress.builder()
            .buildingName("Previous address Building")
            .addressNumber("14")
            .streetName("Low Street")
            .district("East Field")
            .town("Dover")
            .county("Kent")
            .postcode("K23 9QW")
            .from("2010-11-19")
            .status(AddressStatus.builder()
                .code("B")
                .description("Bail")
                .build())
            .build();
    }

}
