@Search
Feature: Search Page


  Scenario: Initial search page is displayed

    When the user is on the National Search page
    Then the user must see an input field for a search phrase
    And see a link to "Add a new person"
    And see a link to "Tips for getting better results"
    And see a link to "previous search"
    And see a link to "feedback"

  Scenario: Search for an single offender
    Given the user is on the National Search page
    And that the offender has the following data from json file "X00001" in elastic search
    When I search for "X00001"
    And the search results are returned
    Then I see "1" search result(s)
    And The offender with crn "X00001" in the results

  Scenario: Search for multiple offenders
    Given the user is on the National Search page
    And that the offender has the following data from json file "multipleResults" in elastic search
    When I search for "smith"
    And the search results are returned
    Then I see "10" search result(s)

  Scenario: Suggestions
    Given the user is on the National Search page
    And that the offender has the following data from json file "X00001" in elastic search
    When I search for "X00001"
    And the search results are returned
    Then I see "X00002 X00003" as a suggested search alternative

  Scenario: Previous search phrase is saved
    Given the user is on the National Search page
    And that the offender has the following data from json file "X00001" in elastic search
    And I search for "X00001"
    And the search results are returned
    When the user is on the National Search page
    Then search field is pre filled with "X00001"
    And the search results are returned
    And The offender with crn "X00001" in the results

   Scenario: Filters will be displayed
     Given the user is on the National Search page
     And that the offender has the following data from json file "multipleResults" in elastic search
     When I search for "smith"
     And the search results are returned
     Then I see my providers filter
     And I see other providers filter



