@Parom1
Feature: Parole report Intervention UI

  Background:
    Given that the Delius user is on the "Interventions" page within the Parole Report

  Scenario: Delius user wants to enter intervention details for a prisoner in his parole report

    Given they want to enter the intervention details for a prisoner
    When  they enter the following information
      | Detail the interventions the prisoner has completed | Some interventions that the prisoner has completed |
      | Interventions analysis                               | Some interventions analysis text                    |
    Then the following information should be saved in the report
      | interventionsDetail | Some interventions that the prisoner has completed |
      | interventionsSummary | Some interventions analysis text |

  Scenario: Delius user wants to leave the "Interventions" screen without putting any details in the free text fields

    Given the user does not any enter any characters in the free text fields on the page
    When  they select the "Continue" button
    Then  the following error messages are displayed
      | Detail the interventions the prisoner has completed | Detail the interventions the prisoner has completed |
      | Interventions analysis                               | Enter the interventions analysis                     |

  Scenario: Delius user wants to continue entering Prisoner details in the Parole report

    When Delius User completes the "Interventions" UI within the Parole Report
    Then  the user should be directed to the "Prison sentence plan and response" UI

  Scenario: Delius user wants to close the report

    When  they select the "Close" button
    Then  the user should be directed to the "Draft report saved" UI
