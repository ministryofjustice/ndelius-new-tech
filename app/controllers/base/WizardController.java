package controllers.base;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import controllers.ParamsValidator;
import data.base.WizardData;
import data.viewModel.PageStatus;
import helpers.Encryption;
import helpers.InvalidCredentialsException;
import helpers.JsonHelper;
import interfaces.OffenderApi;
import lombok.val;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.Logger;
import play.api.i18n.MessagesProvider;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.typedmap.TypedMap;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;
import play.twirl.api.Txt;
import scala.Function1;
import scala.compat.java8.functionConverterImpls.FromJavaFunction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static controllers.SessionKeys.OFFENDER_API_BEARER_TOKEN;
import static helpers.FluentHelper.content;

public abstract class WizardController<T extends WizardData> extends Controller implements ParamsValidator {

    private final List<String> encryptedFields;
    private final Environment environment;

    protected final Function1<String, String> viewEncrypter;
    protected final Form<T> wizardForm;
    protected final WebJarsUtil webJarsUtil;
    protected final Function<String, String> encrypter;
    protected final Function<String, String> decrypter;
    protected final HttpExecutionContext ec;
    protected final Config configuration;
    protected final MessagesApi messagesApi;
	protected final OffenderApi offenderApi;

    protected WizardController(HttpExecutionContext ec,
                               WebJarsUtil webJarsUtil,
                               Config configuration,
                               Environment environment,
                               MessagesApi messagesApi,
                               EncryptedFormFactory formFactory,
                               Class<T> wizardType,
                               OffenderApi offenderApi) {

        this.ec = ec;
        this.webJarsUtil = webJarsUtil;
        this.environment = environment;
        this.configuration = configuration;
		this.messagesApi = messagesApi;
		this.offenderApi = offenderApi;

        wizardForm = formFactory.form(wizardType, this::decryptParams);
        encryptedFields = newWizardData().encryptedFields().map(Field::getName).collect(Collectors.toList());

        val paramsSecretKey = configuration.getString("params.secret.key");

        encrypter = plainText -> Encryption.encrypt(plainText, paramsSecretKey).orElse("");
        decrypter = encrypted -> Encryption.decrypt(encrypted, paramsSecretKey).orElse("");

        viewEncrypter = new FromJavaFunction(encrypter); // Use Scala functions in the view.scala.html markup
    }

    public final CompletionStage<Result> wizardGet(Http.Request request) {

        return initialParams(request).thenApplyAsync(params -> {

            val errorMessage = params.get("errorMessage");

            if (Strings.isNullOrEmpty(errorMessage)) {

                val boundForm = wizardForm.bind(Lang.defaultLang(), TypedMap.empty(), params);
                val thisPage = boundForm.value().map(WizardData::getPageNumber).orElse(1);
                val pageStatuses = getPageStatuses(boundForm.value(), thisPage, null);

                var result = ok(renderPage(request, thisPage, boundForm, pageStatuses));
                if (params.containsKey(OFFENDER_API_BEARER_TOKEN) && params.get(OFFENDER_API_BEARER_TOKEN) != null) {
                    return result.addingToSession(request, OFFENDER_API_BEARER_TOKEN, params.get(OFFENDER_API_BEARER_TOKEN));
                } else return result;

            } else {

                return badRequest(renderErrorMessage(errorMessage, request));
            }

        }, ec.current()).
                handleAsync((result, e) -> Optional.ofNullable(e).map(throwable -> {
                    Logger.error("Wizard Get failure", throwable);

                    if (throwable instanceof CompletionException &&
                            throwable.getCause() instanceof InvalidCredentialsException) {

                        return ((InvalidCredentialsException) throwable.getCause()).getErrorResult();
                    }

                    return internalServerError(renderErrorMessage("We are unable to process your request. Please try again later.", request));
                }).orElse(result), ec.current());
    }

    public final CompletionStage<Result> wizardPost(Http.Request request) {

        val boundForm = wizardForm.bindFromRequest(request);
        val thisPage = boundForm.value().map(WizardData::getPageNumber).orElse(1);
        val visitedPages = new StringBuilder();
        val pageStatuses = getPageStatuses(boundForm.value(), thisPage, visitedPages);

        if (boundForm.hasErrors()) {

            val errorPage = allErrors(boundForm).stream().map(ValidationError::key).findFirst().
                    flatMap(field -> boundForm.value().flatMap(wizardData -> wizardData.getField(field))).
                    map(WizardData::fieldPage).orElse(thisPage);

            val errorData = new HashMap<String, String>(boundForm.rawData());
            errorData.put("pageNumber", errorPage.toString());
            errorData.put("visitedPages", visitedPages.toString());

            return CompletableFuture.supplyAsync(() -> {
                Logger.debug("Bad data posted to wizard: " + allErrors(boundForm));
                return badRequest(renderPage(request, errorPage, wizardForm.bind(Lang.defaultLang(), TypedMap.empty(), errorData), pageStatuses));
            }, ec.current());

        } else {

            val wizardData = boundForm.get();
            val nextPage = nextPage(wizardData);

            wizardData.setVisitedPages(visitedPages.toString());

            String id;
            if (nextPage < 1 || nextPage > wizardData.totalPages()) {
                id = renderingData(request, wizardData);
            } else {
				wizardData.setPageNumber(nextPage); // Only store real page values as is persisted to Alfresco for re-edit
				id = null;
            }

            return nextPage <= wizardData.totalPages() ?
                    nextPage > 0 ?
                            CompletableFuture.supplyAsync(() -> {
                                Result result = ok(renderPage(request, nextPage, wizardForm.fill(wizardData), pageStatuses));
                                if (id != null) result = result.addingToSession(request, "id", id);
                                return result;
                            }, ec.current()) :
                            cancelledWizard(request, wizardData) :
                    completedWizard(request, wizardData);
        }
    }

    protected CompletionStage<Map<String, String>> initialParams(Http.Request request) { // Overridable in derived Controllers to supplant initial params

        val params = request.queryString().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
        return CompletableFuture.supplyAsync(() -> decryptParams(params), ec.current());
    }

    protected Map<String, String> modifyParams(Map<String, String> params, Consumer<String> paramEncrypter) {

        return params;
    }

    protected Integer nextPage(T wizardData) {  // Overridable in derived Controllers jump pages based on content

        return Optional.ofNullable(wizardData.getJumpNumber()).orElse(wizardData.getPageNumber() + 1);
    }

    protected abstract String baseViewName();

    protected abstract CompletionStage<Result> completedWizard(Http.Request request, T wizardData);

    protected abstract CompletionStage<Result> cancelledWizard(Http.Request request, T wizardData);

    protected final Result wizardFailed(Http.Request request, T wizardData) {

        val formData = wizardForm.fill(wizardData);

        return badRequest(renderPage(request, wizardData.totalPages(), formData,
                getPageStatuses(formData.value(), wizardData.getPageNumber(), null)));
    }

    protected String renderingData(Http.Request request, T wizardData) {
        val id = request.session().get("id").orElseGet(() -> UUID.randomUUID().toString());

        val eventData = new HashMap<String, Object>()
        {
            {
                put("username", wizardData.getOnBehalfOfUser());
                put("sessionId", id);
                put("pageNumber", wizardData.getPageNumber());
                put("dateTime", new Date());
            }
        };

        Logger.info("Session: " + eventData.get("sessionId") + " - Page: " + eventData.get("pageNumber") + " - " + eventData.get("dateTime"));
        return id;
    }

    protected BiFunction<Form<T>, Map<Integer, PageStatus>, Content> formRenderer(Http.Request request, String viewName) {
        val render = getRenderMethod(viewName, Form.class, Function1.class, Map.class, WebJarsUtil.class, Environment.class, Config.class, Http.Request.class, MessagesProvider.class);

        return (form, pageStatuses) -> {

            renderingData(request, form.value().orElseGet(this::newWizardData));

            return render.map(method -> invokeContentMethod(method, form, viewEncrypter, pageStatuses, webJarsUtil, environment, configuration, request, messagesApi.preferred(request))).orElseGet(() -> {
                val errorMessage = new StringBuilder();

                errorMessage.append("Form Renderer Error\n");
                errorMessage.append(viewName);
                errorMessage.append("\n");
                errorMessage.append(form.rawData());

                Logger.error(errorMessage.toString());
                return renderErrorMessage(errorMessage.toString(), request);
            });
        };
    }

    protected Content renderErrorMessage(String errorMessage, Http.Request request) {

        return Txt.apply(errorMessage);
    }

    protected T newWizardData() {

        try {
            return wizardForm.getBackedType().newInstance();

        } catch (InstantiationException | IllegalAccessException ex) {

            Logger.error("Unable to instantiate new Wizard Data", ex);
            return null;
        }
    }

    private Map<Integer, PageStatus> getPageStatuses(Optional<T> boundForm, int thisPage, StringBuilder jsonVisitedPages) {

        val errorPages = boundForm.map(value ->
                value.validateAll().stream().map(ValidationError::key).map(value::getField).
                        filter(Optional::isPresent).map(Optional::get).map(WizardData::fieldPage).distinct()
        ).orElseGet(Stream::empty).collect(Collectors.toList());

        val previouslyVisited = (List<Integer>)JsonHelper.readValue(boundForm.map(WizardData::getVisitedPages).
                        flatMap(value -> isNullOrEmpty(value) ? Optional.empty() : Optional.of(value)).
                        orElse("[]"),
                List.class);

        val visitedPages = Stream.concat(Stream.of(thisPage), previouslyVisited.stream()).distinct().sorted().collect(Collectors.toList());

        if (jsonVisitedPages != null) {
            jsonVisitedPages.append(JsonHelper.stringify(visitedPages));
        }

        return IntStream.rangeClosed(1, newWizardData().totalPages()).boxed().collect(Collectors.toMap(
                Function.identity(), page -> new PageStatus(visitedPages.contains(page), !errorPages.contains(page))));
    }

    private String viewPageName(int pageNumber) {

        return baseViewName() + pageNumber;
    }

    private Content renderPage(Http.Request request, int pageNumber, Form<T> formContent, Map<Integer, PageStatus> pageStatuses) {

        return formRenderer(request, viewPageName(pageNumber)).apply(formContent, pageStatuses);
    }

    private Map<String, String> decryptParams(Map<String, String> params) {

        Consumer<String> paramEncrypter = key -> Optional.ofNullable(params.get(key)).map(value -> params.put(key, encrypter.apply(value)));

        val pageNumber = Optional.ofNullable(params.get("pageNumber")).orElse("");
        val jumpNumber = Optional.ofNullable(params.get("jumpNumber")).orElse("");

        final BiFunction<Map<String, String>, Consumer<String>, Map<String, String>> modifier =         // Don't modify if submission was from the feedback
                pageNumber.equals(jumpNumber) ? (ignored1, ignored2) -> params : this::modifyParams;    // form - only time jump an page are the same

        modifier.apply(params, paramEncrypter).keySet().stream().filter(encryptedFields::contains).forEach(field ->
                params.put(field, decrypter.apply(params.get(field)))
        );


        return params;
    }

    private Optional<Method> getRenderMethod(String viewName, Class<?>... parameterTypes) {

        try {
            return Optional.of(environment.classLoader().loadClass(viewName).getDeclaredMethod("render", parameterTypes));
        }
        catch (ClassNotFoundException | NoSuchMethodException ex) {

            Logger.error("Unable to Render View: " + viewName, ex);
            return Optional.empty();
        }
    }

    private Content invokeContentMethod(Method method, Object... args) {

        try {
            return (Content) method.invoke(null, args);
        }
        catch (IllegalAccessException | InvocationTargetException ex) {

            Logger.error("Unable to Invoke Method: " + method.getName(), ex);
            return content(ex);
        }
    }

    public static List<ValidationError> allErrors(Form<?> form) {
        return Stream.concat(form.errors().stream(), form.globalErrors().stream()).collect(Collectors.toList());
    }
}
