@Parom1
Feature: Parole Report - Remmendation

  Background:
    Given Delius User is on the "Recommendation" UI within the Parole Report

  Scenario:   Delius user wants to close the Parole Report

  Scenario: User wants to close the report

    When  they select the "Close" button
    Then  the user should be directed to the "Draft report saved" UI

  Scenario: User wants to continuing writing the parole report

    Given that the "I confirm that I have had sight of the On/post Tariff Parole Custody Report and liaised with the Prison Offender Manager and their views around the assessment and recommendation have been taken into account in this report." is ticked
    And  they enter the following information
      | What is your recommendation? | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. |
    When  they select the "Continue" button
    Then  the user should be directed to the "Oral hearing" UI

  Scenario: Delius user wants to enter Recommendation details for an offender within their Parole report

    Given  they enter the following information
      | What is your recommendation? | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. |
    Then this information should be saved in the report


  Scenario: User does not enter a recommendation

    Given that the user enters no information on the page
    When  they select the "Continue" button
    Then  the following error messages are displayed
      | I confirm that I have had sight of the On/post Tariff Parole Custody Report and liaised with the Prison Offender Manager and their views around the assessment and recommendation have been taken into account in this report. | You must confirm that you have had sight of the On/post Tariff Parole Custody Report and liaised with the Prison Offender Manager and their views around the assessment and recommendation have been taken into account in this report. |
      | What is your recommendation?                                                                                                                                                                                                   | Enter your recommendation                                                                                                                                                                                                               |
