Feature: Short Format Pre-sentence Report - Risk assessment

  Background: Delius user is on the "Risk assessment" UI within the Short Format Pre-sentence Report
    Given that the Delius user is on the "Risk assessment" page within the Short Format Pre-sentence Report

  Scenario: Delius user does not complete the "Risk assessment" required fields

    When they select the "Continue" button
    Then the following error messages are displayed
      | Likelihood of further offending  | Enter the likelihood of further offending  |
      | Risk of serious harm             | Enter the risk of serious harm             |
      | Response to previous supervision | Enter the response to previous supervision |

  Scenario: Delius users wants more information to what they should include in the offender's report

    Given that the Delius user is unclear to what information they need to add to the "Likelihood of further offending" free text field
    And they select "What to include" hyperlink
    Then the UI should expand to show additional content to the end user

  Scenario: Delius users wants more information to what they should include in the offender's report

    Given that the Delius user is unclear to what information they need to add to the "Risk of serious harm" free text field
    And they select "What to include" hyperlink
    Then the UI should expand to show additional content to the end user

  Scenario: Delius users wants more information to what they should include in the offender's report

    Given that the Delius user is unclear to what information they need to include for the "Response to previous supervision" radio group
    And they select "What to include" hyperlink within the radio group
    Then the UI should expand to show additional content within a radio group to the end user

  Scenario: Delius user completes all options on the "Risk assessment" UI

    Given they select the radio button with id "previousSupervisionResponse_Good"
    When they enter the following information
      | Likelihood of further offending                                | Some likelihood of further offending text            |
      | Risk of serious harm                                           | Some risk of serious harm text                       |
      | Add additional details on previous supervision (if applicable) | Some additional details on previous supervision text |

    Then the following information should be saved in the report
      | likelihoodOfReOffending       | Some likelihood of further offending text            |
      | riskOfSeriousHarm             | Some risk of serious harm text                       |
      | previousSupervisionResponse   | Good                                                 |
      | additionalPreviousSupervision | Some additional details on previous supervision text |

  Scenario: Delius user wants to continue writing the Short Format Pre-sentence Report

    Given Delius User completes the "Risk assessment" UI within the Short Format Pre-sentence Report
    Then the user should be directed to the "Proposal" UI