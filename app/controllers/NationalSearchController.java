package controllers;

import interfaces.Search;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static helpers.JsonHelper.okJson;

public class NationalSearchController extends Controller {

    private final views.html.nationalSearch template;
    private final Search elasticSearch;

    @Inject
    public NationalSearchController(views.html.nationalSearch template, Search elasticSearch) {
        this.template = template;
        this.elasticSearch = elasticSearch;
    }

    public Result index() {
        return ok(template.render());
    }

    public Result searchOffender(String searchTerm) {
        if ("blank".equals(searchTerm.toLowerCase())) {
            return ok("[]").as("application/json");
        }

        return okJson(elasticSearch.search(searchTerm));
    }

}
