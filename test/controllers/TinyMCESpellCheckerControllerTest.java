package controllers;

import lombok.val;
import org.junit.Test;
import play.api.libs.json.Json;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

public class TinyMCESpellCheckerControllerTest extends WithApplication {

    @Test
    public void testMisspelledWordsReturnsValidSuggestions() {
        val requestJson = "{\"id\":\"1\",\"params\":{\"words\":[\"speeling\", \"misteke\"]}}";
        val formData = Json.parse(requestJson);
        val request = new Http.RequestBuilder().method(POST).bodyJson(formData).uri("/spellcheck");

        val result = route(app, addCSRFToken(request));
        val content = Helpers.contentAsString(result);
        assertEquals(OK, result.status());

        val expectedSuggestions = asNormalisedJson("{\"result\":{\"words\":{\"speeling\":[\"Spelling\",\"Speeding\",\"Peeling\",\"Steeling\",\"Spieling\"],\"misteke\":[\"mistake\",\"mist eke\"]}}}");
        assertEquals(expectedSuggestions, content);
    }

    @Test
    public void testNoMisspelledWordsReturnsValidSuggestions() {
        val requestJson = "{\"id\":\"1\",\"params\":{\"words\":[\"spelling\", \"mistake\"]}}";
        val formData = Json.parse(requestJson);
        val request = new Http.RequestBuilder().method(POST).bodyJson(formData).uri("/spellcheck");

        val result = route(app, addCSRFToken(request));
        val content = Helpers.contentAsString(result);
        assertEquals(OK, result.status());

        val expectedSuggestions = asNormalisedJson("{ \"result\" : {}}");
        assertEquals(expectedSuggestions, content);
    }

    @Test
    public void testNoWordsReturnsValidSuggestions() {
        val requestJson = "{\"id\":\"1\",\"params\":{\"words\":[]}}";
        val formData = Json.parse(requestJson);
        val request = new Http.RequestBuilder().method(POST).bodyJson(formData).uri("/spellcheck");

        val result = route(app, addCSRFToken(request));
        val content = Helpers.contentAsString(result);
        assertEquals(OK, result.status());

        val expectedSuggestions = asNormalisedJson("{ }");
        assertEquals(expectedSuggestions, content);
    }

    private String asNormalisedJson(String json) {
        return Json.parse(json).toString();
    }
}
