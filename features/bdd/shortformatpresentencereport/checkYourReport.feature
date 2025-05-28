@SFR
Feature: Short Format Pre-sentence Report - Check your report

  Scenario: Delius user has completed all the relevant fields for "Offence details" UI

    Given that the Delius user is on the "Offence details" page within the Short Format Pre-sentence Report
    And Delius User completes the "Offence details" UI within the Short Format Pre-sentence Report
    When they are select "Check your report" link on the navigation menu
    Then the button for "Offence details" must display "SAVED"

  Scenario: Delius user has visited the "Offence details" UI but not completed the relevant fields

    Given that the Delius user is on the "Offence details" page within the Short Format Pre-sentence Report
    When they are select "Check your report" link on the navigation menu
    Then the button for "Offence details" must display "INCOMPLETE"

  Scenario: Delius user has not visited the "Offence analysis" UI

    Given that the Delius user is on the "Offence details" page within the Short Format Pre-sentence Report
    When they are select "Check your report" link on the navigation menu
    Then the button for "Offence analysis" must display "NOT STARTED"
