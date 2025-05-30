package controllers;

import data.Params;
import data.WordsRequested;
import helpers.JsonHelper;
import lombok.val;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.SpellcheckService;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class TinyMCESpellCheckerController extends Controller {

    private final FormFactory formFactory;
    private final Form<WordsRequested> wordsRequestedForm;
    private final SpellcheckService spellcheckService;

    @Inject
    public TinyMCESpellCheckerController(FormFactory formFactory, SpellcheckService spellcheckService) {
        this.formFactory = formFactory;
        this.wordsRequestedForm = formFactory.form(WordsRequested.class);
        this.spellcheckService = spellcheckService;
    }

    public Result findSpellings(Http.Request request) {
        val wordsRequested = wordsRequestedForm.bindFromRequest(request).get();
        Optional<Params> params = Optional.ofNullable(wordsRequested.getParams());
        return params
                .map(result -> ok(Json.parse(spellcheckService.getSpellCheckSuggestions(joinToSingleText(wordsRequested.getParams().getWords())))))
                .orElse(ok(Json.parse("{ }")));
    }

    private String joinToSingleText(Map<String, String> strings) {
        return String.join(" ", strings.values().toArray(new String[0]));
    }
}
