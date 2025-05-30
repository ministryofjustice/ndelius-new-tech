package services;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpellcheckServiceTest {
    private SpellcheckService spellcheckService;

    @Before
    public void setUp() {
        spellcheckService= new SpellcheckService();
    }

    @Test
    public void testThatMisspelledWordsReturnSuggestions() {
        String wordsToCheck = "speeling misteke";
        String spellcheckSuggestionsString = spellcheckService.getSpellCheckSuggestions(wordsToCheck);

        String expectedSuggestions = "{ \"result\" : { \"words\" : { \"speeling\" : [\"Spelling\", \"Speeding\", \"Peeling\", \"Steeling\", \"Spieling\"],\"misteke\" : [\"mistake\", \"mist eke\"] } } }";
        assertThat(spellcheckSuggestionsString).isEqualTo(expectedSuggestions);
    }

    @Test
    public void testThatWhenNoMisspelledWordsReturnsEmptySuggestions() {
        String wordsToCheck = "spelling mistake";
        String spellcheckSuggestionsString = spellcheckService.getSpellCheckSuggestions(wordsToCheck);

        String expectedSuggestions = "{ \"result\" : {}}";
        assertThat(spellcheckSuggestionsString).isEqualTo(expectedSuggestions);
    }
}