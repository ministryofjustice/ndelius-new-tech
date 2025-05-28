package services;

import com.google.inject.Inject;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpellcheckService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpellcheckService.class);

    @Inject
    public SpellcheckService() {}

    public String getSpellCheckSuggestions(String textToSpellCheck) {
        StringBuilder spellingMistakes = new StringBuilder();
        List<RuleMatch> matches = getRuleMatches(textToSpellCheck);
        if(matches != null && !matches.isEmpty()) {
            matches.forEach(
                ruleMatch -> {
                    spellingMistakes.append("\"").append(textToSpellCheck, ruleMatch.getFromPos(), ruleMatch.getToPos()).append("\" : ");
                    spellingMistakes.append(ruleMatch.getSuggestedReplacements().stream().map(str -> "\"" + str + "\"").collect(Collectors.toList()));
                    spellingMistakes.append(",");
                }
            );
        }
        return constructResponse(spellingMistakes);
    }


    private List<RuleMatch> getRuleMatches(String textToSpellCheck) {
        JLanguageTool languageTool = createLanguageTool();
        List<RuleMatch> matches = Collections.emptyList();
        try {
            matches = languageTool.check(textToSpellCheck);
        } catch (IOException e) {
            LOGGER.error("Error occurred whilst checking spelling : ", e);
        }
        return matches;
    }

    private JLanguageTool createLanguageTool() {
        JLanguageTool languageTool = new JLanguageTool(BritishEnglish.getInstance());
        disableGrammarChecking(languageTool);
        return languageTool;
    }

    private void disableGrammarChecking(JLanguageTool languageTool) {
        languageTool.getAllRules().forEach(
                rule -> {
                    if (!rule.isDictionaryBasedSpellingRule()) {
                        languageTool.disableRule(rule.getId());
                    }
                }

        );
    }

    private String constructResponse(StringBuilder spellingMistakes) {
        val spellingMistakesValue = spellingMistakes.toString();
        if(StringUtils.isNotBlank(spellingMistakesValue)) {
            val spellingMistakesNoEndComma = spellingMistakesValue.substring(0, spellingMistakesValue.lastIndexOf(","));
            return String.format("{ \"result\" : { \"words\" : { %s } } }", spellingMistakesNoEndComma);
        }
        return "{ \"result\" : {}}";
    }
}