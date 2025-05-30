package interfaces;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.val;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static helpers.DateTimeHelper.formatDateTime;
import static helpers.FluentHelper.not;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public interface OffenderApi {

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class Offender {
        private String firstName;
        private String surname;
        private List<String> middleNames;
        private String dateOfBirth;
        private Map<String, String> otherIds;
        private ContactDetails contactDetails;
        private String gender;

        public String displayName() {

            String middleName = ofNullable(middleNames).flatMap(names -> names.stream().findFirst()).orElse(null);
            return joinList(" ", asList(firstName, middleName, surname));
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class ContactDetails {
        private List<OffenderAddress> addresses;

        public Optional<OffenderAddress> mainAddress() {
            return ofNullable(addresses)
                .flatMap(offenderAddresses -> offenderAddresses.stream()
                    .filter(address -> ofNullable(address.getStatus()).isPresent())
                    .filter(address -> "M".equals(address.getStatus().getCode().toUpperCase()))
                    .findFirst());
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class OffenderAddress {
        private String buildingName;
        private String addressNumber;
        private String streetName;
        private String district;
        private String town;
        private String county;
        private String postcode;
        private String from;
        private String to;
        private AddressStatus status;

        public String render() {

            val address = asList(
                    buildingName,
                    joinList(" ", asList(addressNumber, streetName)),
                    district,
                    town,
                    county,
                    postcode);

            return joinList("\n", address);
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class AddressStatus {
        private String code;
        private String description;
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class CourtAppearances {
        @Builder.Default private List<CourtAppearance> items = ImmutableList.of();

        public Optional<CourtAppearance> findForCourtReportId(Long courtReportId) {

            return items.stream()
                .filter(courtAppearance -> Optional.ofNullable(courtAppearance.courtReports).isPresent())
                .filter(courtAppearance ->
                    courtAppearance.courtReports.stream()
                        .anyMatch(report -> report.getCourtReportId().equals(courtReportId)))
                .findFirst();
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class CourtAppearance {
        private Long courtAppearanceId;
        private String appearanceDate;
        private Court court;
        @Builder.Default private List<CourtReport> courtReports = ImmutableList.of();
        @Builder.Default private List<String> offenceIds = ImmutableList.of();

        public String mainOffenceId() {
            return offenceIds.stream()
                .filter(s -> s.startsWith("M"))
                .findFirst()
                .orElse("");
        }

        public List<String> otherOffenceIds() {
            return offenceIds.stream()
                .filter(s -> s.startsWith("A"))
                .collect(toList());
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class Court {
        private Long courtId;
        private String courtName;
        private String locality;
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class CourtReport {
        private Long courtReportId;
        private Court requiredByCourt;
        private String dateRequired;
    }


    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class Offences {
        @Builder.Default private List<Offence> items = ImmutableList.of();

        public String mainOffenceDescriptionForId(String mainOffenceId) {
            return items.stream()
                .filter(offence -> offence.mainOffence)
                .filter(offence -> Optional.ofNullable(offence.getOffenceId()).isPresent())
                .filter(offence -> offence.getOffenceId().equals(mainOffenceId))
                .findFirst()
                .map(Offence::offenceDescription)
                .orElse("NO MAIN OFFENCE FOUND");
        }

        public String otherOffenceDescriptionsForIds(List<String> otherOffenceIds) {
            return items.stream()
                .filter(not(Offence::getMainOffence))
                .filter(offence -> Optional.ofNullable(offence.getOffenceId()).isPresent())
                .filter(offence -> otherOffenceIds.contains(offence.getOffenceId()))
                .map(Offence::offenceDescription)
                .collect(joining("<br>"));
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class Offence {
        private String offenceId;
        private Boolean mainOffence;
        private String offenceDate;
        private OffenceDetail detail;
        private Long offenceCount;

        public String offenceDescription() {
            return Optional.ofNullable(offenceDate)
                .map(ignored -> String.format("%s (%s) - %s",
                    detail.getSubCategoryDescription(),
                    detail.getCode(),
                    formatDateTime(offenceDate)))
                .orElse(String.format("%s (%s)",
                    detail.getSubCategoryDescription(),
                    detail.getCode()));
        }

        public String offenceShortDescription() {
            return Optional.ofNullable(offenceDate)
                .map(ignored -> String.format("%s - %s", descriptionWithCount(), formatDateTime(offenceDate)))
                .orElse(descriptionWithCount());
        }

        private String descriptionWithCount() {
            return hasMultipleOffences() ? String.format("%s x %d", detail.getSubCategoryDescription(), offenceCount): detail.getSubCategoryDescription();
        }
        private boolean hasMultipleOffences() {
            return Optional.ofNullable(offenceCount).map(count -> count > 1).orElse(false);
        }
    }

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    class OffenceDetail {
        private String code;
        private String description;
        private String mainCategoryCode;
        private String mainCategoryDescription;
        private String subCategoryCode;
        private String subCategoryDescription;
    }

    @Value
    @Builder
    @Jacksonized
    class InstitutionalReport {
        private Conviction conviction;
        private Sentence sentence;
    }

    @Value
    @Builder
    @Jacksonized
    class Sentence {
         private String description;
         private Long originalLength;
         private String originalLengthUnits;

        public String descriptionAndLength() {
            return String.format("%s, %s %s.", description, originalLength, originalLengthUnits);
        }
    }

    @Value
    @Builder
    @Jacksonized
    class Conviction {
        private String convictionDate;
        private List<Offence> offences;

        public String mainOffenceDescription() {
            return offences.stream()
                .filter(offence -> offence.mainOffence)
                .findFirst()
                .map(Offence::offenceShortDescription)
                .orElse("");
        }

        public List<String> additionalOffenceDescriptions() {

            return ImmutableList.<String>builder()
                .addAll(descriptionsForAdditionalOffencesWithDates())
                .addAll(descriptionsForAdditionalOffencesWithoutDates())
                .build();
        }

        public String allOffenceDescriptions() {
            val descriptions = ImmutableList.<String>builder()
                .add(mainOffenceDescription())
                .addAll(additionalOffenceDescriptions()).build();

            return joinList("<br>", descriptions);
        }

        private List<String> descriptionsForAdditionalOffencesWithDates() {
            return offences.stream()
                .filter(not(Offence::getMainOffence))
                .filter(not(offence -> isBlank(offence.offenceDate)))
                .sorted(Comparator.comparing(Offence::getOffenceDate).reversed())
                .map(Offence::offenceShortDescription)
                .collect(toList());
        }

        private List<String> descriptionsForAdditionalOffencesWithoutDates() {
            return offences.stream()
                .filter(not(Offence::getMainOffence))
                .filter(offence -> isBlank(offence.offenceDate))
                .map(Offence::offenceShortDescription)
                .collect(toList());
        }
    }

    static String joinList(String delimiter, List<String> list) {
        return String.join(delimiter,
            list.stream()
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList()));
    }

    CompletionStage<String> logon(String username);

    CompletionStage<HealthCheckResult> isHealthy();

    CompletionStage<Offender> getOffenderByCrn(String bearerToken, String crn);

    CompletionStage<CourtAppearances> getCourtAppearancesByCrn(String bearerToken, String crn);

    CompletionStage<CourtReport> getCourtReportByCrnAndCourtReportId(String bearerToken, String crn, String courtReportId);

    CompletionStage<Offences> getOffencesByCrn(String bearerToken, String crn);
}
