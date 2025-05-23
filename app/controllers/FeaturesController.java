package controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;

public class FeaturesController extends Controller {

    private final views.html.features template;

    @Inject
    public FeaturesController(views.html.features template) {
        this.template = template;
    }

    public Result index(Http.Request request) {
        return Results.ok(template.render(request));
    }

}
