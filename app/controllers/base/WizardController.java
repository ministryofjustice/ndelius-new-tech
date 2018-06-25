package controllers.base;

import com.typesafe.config.Config;
import controllers.ParamsValidator;
import data.base.WizardData;
import data.viewModel.PageStatus;
import helpers.Encryption;
import helpers.JsonHelper;
import interfaces.AnalyticsStore;
import interfaces.OffenderApi;
import lombok.val;
import org.joda.time.DateTime;
import org.webjars.play.WebJarsUtil;
import play.Environment;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Content;
import play.twirl.api.Txt;
import scala.Function1;
import scala.compat.java8.functionConverterImpls.FromJavaFunction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static helpers.DateTimeHelper.*;
import static helpers.FluentHelper.content;
import static helpers.JwtHelper.principal;
import static java.time.Clock.systemUTC;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class WizardController<T extends WizardData> extends Controller {

    private final AnalyticsStore analyticsStore;
    private final List<String> encryptedFields;
    private final Environment environment;

    protected final Function1<String, String> viewEncrypter;
    protected final Form<T> wizardForm;
    protected final WebJarsUtil webJarsUtil;
    protected final Function<String, String> encrypter;
    protected final Function<String, String> decrypter;
    protected final HttpExecutionContext ec;
    protected final Duration userTokenValidDuration;
    protected final ParamsValidator paramsValidator;
    protected final OffenderApi offenderApi;

    protected WizardController(HttpExecutionContext ec,
                               WebJarsUtil webJarsUtil,
                               Config configuration,
                               Environment environment,
                               AnalyticsStore analyticsStore,
                               EncryptedFormFactory formFactory,
                               Class<T> wizardType,
                               ParamsValidator paramsValidator,
                               OffenderApi offenderApi) {

        this.ec = ec;
        this.webJarsUtil = webJarsUtil;
        this.environment = environment;
        this.analyticsStore = analyticsStore;
        this.paramsValidator = paramsValidator;
        this.offenderApi = offenderApi;

        wizardForm = formFactory.form(wizardType, this::decryptParams);
        encryptedFields = newWizardData().encryptedFields().map(Field::getName).collect(Collectors.toList());

        val paramsSecretKey = configuration.getString("params.secret.key");

        encrypter = plainText -> Encryption.encrypt(plainText, paramsSecretKey).orElse("");
        decrypter = encrypted -> Encryption.decrypt(encrypted, paramsSecretKey).orElse("");

        viewEncrypter = new FromJavaFunction(encrypter); // Use Scala functions in the view.scala.html markup

        userTokenValidDuration = configuration.getDuration("params.user.token.valid.duration");
    }

    public final CompletionStage<Result> wizardGet() {

        val encryptedUsername = request().queryString() != null && request().queryString().get("user") != null ? request().queryString().get("user")[0] : "";
        val username = decrypter.apply(encryptedUsername);

        val encryptedEpochRequestTimeMills = request().queryString() != null && request().queryString().get("t") != null ? request().queryString().get("t")[0] : "";
        val epochRequestTimeMills = decrypter.apply(encryptedEpochRequestTimeMills);

        val encryptedCrn= request().queryString() != null && request().queryString().get("crn") != null ? request().queryString().get("crn")[0] : "";
        val crn = decrypter.apply(encryptedCrn);

        final Supplier<CompletionStage<Result>> renderedPage = () -> offenderApi.logon(username)
            .thenApplyAsync(bearerToken -> {
                Logger.info("AUDIT:{}: WizardController: Successful logon for user {}", principal(bearerToken), username);
                return bearerToken;

            }, ec.current())
            .thenCompose(bearerToken -> offenderApi.getOffenderByCrn(bearerToken, crn))
            .thenCombineAsync(initialParams(), (offenderDetails, params) -> {
                val errorMessage = params.get("errorMessage");

                if (isNullOrEmpty(errorMessage)) {

                    if (offenderDetails.get("firstName") != null) {
                        params.put("name", String.format("%s %s", offenderDetails.get("firstName"), offenderDetails.get("surname")));
                    }
                    if (offenderDetails.get("dateOfBirth") != null) {
                        params.put("dateOfBirth", prettyPrint((String) offenderDetails.get("dateOfBirth")));
                    }
                    if (offenderDetails.get("dateOfBirth") != null) {
                        params.put("age", String.format("%d", calculateAge((String) offenderDetails.get("dateOfBirth"), systemUTC())));
                    }
                    if (offenderDetails.get("otherIds") != null && !isBlank( ((Map<String, String>)offenderDetails.get("otherIds")).get("pncNumber"))) {
                        params.put("pnc", ((Map<String, String>)offenderDetails.get("otherIds")).get("pncNumber"));
                        params.put("pncSupplied", "true");
                    } else {
                        params.put("pncSupplied", "false");
                    }

                    if (offenderDetails.get("contactDetails") != null && !((List<Object>) ((Map<String, Object>) offenderDetails.get("contactDetails")).get("addresses")).isEmpty()) {
                        val currentAddress = ((List<Map<String, Object>>) ((Map<String, Object>) offenderDetails.get("contactDetails")).get("addresses")).stream()
                            .sorted(Comparator.comparing(address -> convert((String) address.get("from"))))
                            .collect(Collectors.toList()).get(0);
                        String singleLineAddress = ((currentAddress.get("buildingName") == null) ? "" : currentAddress.get("buildingName") + "\n") +
                            ((currentAddress.get("addressNumber") == null) ? "" : currentAddress.get("addressNumber") + " ") +
                            ((currentAddress.get("streetName") == null) ? "" : currentAddress.get("streetName") + "\n") +
                            ((currentAddress.get("district") == null) ? "" : currentAddress.get("district") + "\n") +
                            ((currentAddress.get("townCity") == null) ? "" : currentAddress.get("townCity") + "\n") +
                            ((currentAddress.get("county") == null) ? "" : currentAddress.get("county") + "\n") +
                            ((currentAddress.get("postcode") == null) ? "" : currentAddress.get("postcode") + "\n");
                        params.put("address", singleLineAddress);
                        params.put("addressSupplied", "true");
                    } else {
                        params.put("addressSupplied", "false");
                    }

                    val boundForm = wizardForm.bind(params);
                    val thisPage = boundForm.value().map(WizardData::getPageNumber).orElse(1);
                    val pageStatuses = getPageStatuses(boundForm.value(), thisPage, null);

                    return ok(renderPage(thisPage, boundForm, pageStatuses));

                } else {

                    return badRequest(renderErrorMessage(errorMessage));
                }
            }, ec.current());

        final Runnable errorReporter = () -> Logger.error(String.format("Short format report search request did not receive a valid user (%s) or t (%s)", encryptedUsername, encryptedEpochRequestTimeMills));
        return paramsValidator.invalidCredentials(username, epochRequestTimeMills, errorReporter).
            map(result -> (CompletionStage<Result>) CompletableFuture.completedFuture(result)).
            orElseGet(renderedPage).
            exceptionally(throwable -> {

                Logger.info("AUDIT:{}: Unable to login {}", "anonymous", username);
                Logger.error("Unable to logon to offender API", throwable);

                return internalServerError();
            });


    }

    public final CompletionStage<Result> wizardPost() {

        val boundForm = wizardForm.bindFromRequest();
        val thisPage = boundForm.value().map(WizardData::getPageNumber).orElse(1);
        val visitedPages = new StringBuilder();
        val pageStatuses = getPageStatuses(boundForm.value(), thisPage, visitedPages);

        if (boundForm.hasErrors()) {

            val errorPage = boundForm.allErrors().stream().map(ValidationError::key).findFirst().
                    flatMap(field -> boundForm.value().flatMap(wizardData -> wizardData.getField(field))).
                    map(WizardData::fieldPage).orElse(thisPage);

            val errorData = new HashMap<String, String>(boundForm.rawData());
            errorData.put("pageNumber", errorPage.toString());
            errorData.put("visitedPages", visitedPages.toString());

            return CompletableFuture.supplyAsync(() -> {
                Logger.debug("Bad data posted to wizard: " + boundForm.allErrors());
                return badRequest(renderPage(errorPage, wizardForm.bind(errorData), pageStatuses));
            }, ec.current());

        } else {

            val wizardData = boundForm.get();
            val nextPage = nextPage(wizardData);

            wizardData.setVisitedPages(visitedPages.toString());

            if (nextPage < 1 || nextPage > wizardData.totalPages()) {
                renderingData(wizardData);
            } else {
                wizardData.setPageNumber(nextPage); // Only store real page values as is persisted to Alfresco for re-edit
            }

            return nextPage <= wizardData.totalPages() ?
                    nextPage > 0 ?
                            CompletableFuture.supplyAsync(() -> ok(renderPage(nextPage, wizardForm.fill(wizardData), pageStatuses)), ec.current()) :
                            cancelledWizard(wizardData) :
                    completedWizard(wizardData);
        }
    }

    public final CompletionStage<Result> feedbackPost() {

        return CompletableFuture.supplyAsync(() -> {

            val formData = wizardForm.bindFromRequest();

            return ok(formRenderer(baseViewName() + "Feedback").apply(
                    formData, getPageStatuses(formData.value(), 0, null)));

        }, ec.current());
    }

    protected CompletionStage<Map<String, String>> initialParams() { // Overridable in derived Controllers to supplant initial params

        val params = request().queryString().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));

        return CompletableFuture.supplyAsync(() -> decryptParams(params), ec.current());
    }

    protected Map<String, String> modifyParams(Map<String, String> params, Consumer<String> paramEncrypter) {

        return params;
    }

    protected Integer nextPage(T wizardData) {  // Overridable in derived Controllers jump pages based on content

        return Optional.ofNullable(wizardData.getJumpNumber()).orElse(wizardData.getPageNumber() + 1);
    }

    protected abstract String baseViewName();

    protected abstract CompletionStage<Result> completedWizard(T wizardData);

    protected abstract CompletionStage<Result> cancelledWizard(T wizardData);

    protected final Result wizardFailed(T wizardData) {

        val formData = wizardForm.fill(wizardData);

        return badRequest(renderPage(wizardData.totalPages(), formData,
                getPageStatuses(formData.value(), wizardData.getPageNumber(), null)));
    }

    protected void renderingData(T wizardData) {

        if (isNullOrEmpty(session("id"))) {
            session("id", UUID.randomUUID().toString());
        }

        val feedback = new HashMap<String, Object>()
        {
            {
                put("email", wizardData.getEmail());
                put("rating", wizardData.getRating());
                put("feedback", wizardData.getFeedback());
                put("role", wizardData.getRoleother() == null || wizardData.getRoleother().isEmpty() ? wizardData.getRole() : wizardData.getRoleother());
                put("provider", wizardData.getProvider());
                put("region", wizardData.getRegion());
            }
        };

        val eventData = new HashMap<String, Object>()
        {
            {
                put("username", wizardData.getOnBehalfOfUser());
                put("sessionId", session("id"));
                put("pageNumber", wizardData.getPageNumber());
                put("dateTime", DateTime.now().toDate());
                put("feedback", feedback);
            }
        };

        Logger.info("Session: " + eventData.get("sessionId") + " - Page: " + eventData.get("pageNumber") + " - " + eventData.get("dateTime"));

        analyticsStore.recordEvent(eventData);
    }

    protected BiFunction<Form<T>, Map<Integer, PageStatus>, Content> formRenderer(String viewName) {

        val render = getRenderMethod(viewName, Form.class, Function1.class, Map.class, WebJarsUtil.class, Environment.class);

        return (form, pageStatuses) -> {

            renderingData(form.value().orElseGet(this::newWizardData));

            return render.map(method -> invokeContentMethod(method, form, viewEncrypter, pageStatuses, webJarsUtil, environment)).orElseGet(() -> {

                val errorMessage = new StringBuilder();

                errorMessage.append("Form Renderer Error\n");
                errorMessage.append(viewName);
                errorMessage.append("\n");
                errorMessage.append(form.rawData());

                Logger.error(errorMessage.toString());
                return renderErrorMessage(errorMessage.toString());
            });
        };
    }

    protected Content renderErrorMessage(String errorMessage) {

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

    private Content renderPage(int pageNumber, Form<T> formContent, Map<Integer, PageStatus> pageStatuses) {

        return formRenderer(viewPageName(pageNumber)).apply(formContent, pageStatuses);
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
}
