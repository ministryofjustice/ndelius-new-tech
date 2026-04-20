Feature: Short Format Pre-sentence Report - Sentencing court details

  Background: Delius user is on the "Sentencing court details" UI within the Short Format Pre-sentence Report
    Given that the Delius user is on the "Sentencing court details" page within the Short Format Pre-sentence Report

  Scenario: Delius user deletes pre-populated data in the "Sentencing court details" required fields

    Given they input the following information
      | Court              |  |
      | Local justice area |  |
    And they remove the stored date for "Date of hearing"
    When they select the "Continue" button
    Then  the following error messages are displayed
      | Court              | Enter the court              |
      | Local justice area | Enter the local justice area |
      | Date of hearing    | Enter the date of hearing    |

  Scenario: Delius user completes all options on the "Sentencing court details" UI

    When they input the following information
      | Court              | Some court text              |
      | Local justice area | Some local justice area text |
    And they enter the date "09/08/2018" for "Date of hearing"

    Then the following information should be saved in the report
      | court            | Some court text              |
      | localJusticeArea | Some local justice area text |
      | dateOfHearing    | 09/08/2018                   |

  Scenario: Delius user wants to continue writing the Short Format Pre-sentence Report

    Given Delius User completes the "Sentencing court details" UI within the Short Format Pre-sentence Report
    Then the user should be directed to the "Offence details" UI