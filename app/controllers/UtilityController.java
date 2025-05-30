package controllers;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import helpers.JsonHelper;
import interfaces.DocumentStore;
import interfaces.HealthCheckResult;
import interfaces.OffenderApi;
import interfaces.PdfGenerator;
import lombok.Value;
import lombok.val;
import org.apache.commons.io.FileUtils;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;

public class UtilityController extends Controller {

    @Value
    private static class Definition {
        private String name;
        private boolean aggregate;
    }

    private static Definition definition(String name, boolean aggregate) {
        return new Definition(name, aggregate);
    }

    private final Map<Definition, Supplier<CompletableFuture<HealthCheckResult>>> healthChecks;

    private final Config configuration;
	private final MessagesApi messagesApi;
    private final OffenderApi offenderApi; // Used by searchDb and searchLdap

    @Inject
    public UtilityController(PdfGenerator pdfGenerator,
                             DocumentStore documentStore,
                             OffenderApi offenderApi,
                             Config configuration,
                             MessagesApi messagesApi) {
        this.offenderApi = offenderApi; // Used by searchDb and searchLdap, so stored directly for later, others are closed over below

		healthChecks = ImmutableMap.<Definition, Supplier<CompletableFuture<HealthCheckResult>>>builder().
                put(definition("pdf-generator", true), () -> pdfGenerator.isHealthy().toCompletableFuture()).
                put(definition("document-store", true), () -> documentStore.isHealthy().toCompletableFuture()).
                put(definition("offender-api", true), () -> offenderApi.isHealthy().toCompletableFuture()).
                build();

        this.configuration = configuration;
        this.messagesApi = messagesApi;
    }

    public CompletionStage<Result> healthcheck(boolean details) {
        final Function<HealthCheckResult, Object> statusMapper = details ?
                healthCheckResult -> healthCheckResult
                : healthCheckResult -> toStatus(healthCheckResult.isHealthy());

        val definedFutures = healthChecks.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get() // Initiates a call to the closure code above and requests a fresh future
                )
        );

        final Function<Void, Map<Definition, HealthCheckResult>> buildStatuses = ignored ->
                definedFutures.entrySet().stream().collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().join()    // .join() is only executed when buildStatuses is
                        )                                           // invoked after completion of allHealthFutures
                );                                                  // so no blocking occurs, as all futures are complete

        val allHealthFutures = CompletableFuture.allOf(definedFutures.values().toArray(new CompletableFuture[0]));

        return allHealthFutures.
                thenApply(buildStatuses).
                thenApply(statuses -> buildResult(statuses, statusMapper)).
                thenApply(JsonHelper::okJson);
    }

    private Map<String, Object> buildResult(Map<Definition, HealthCheckResult> statuses, Function<HealthCheckResult, Object> statusMapper) {

        val overallStatus = statuses.entrySet().stream().
                filter(entry -> entry.getKey().isAggregate()).
                map(Map.Entry::getValue).
                allMatch(HealthCheckResult::isHealthy);

        val dependencies = statuses.entrySet().stream().
                collect(Collectors.toMap(entry -> entry.getKey().getName(), entry -> statusMapper.apply(entry.getValue())));

        return ImmutableMap.<String, Object>builder().
                put("status", toStatus(overallStatus)).
                put("dateTime", LocalDateTime.now().toString()).
                put("version", version()).
                put("runtime", runtimeInfo()).
                put("fileSystems", fileSystemDetails()).
                put("localHost", localhost()).
                put("dependencies", dependencies).
                build();
    }

    private static String toStatus(boolean status) {

        return status ? "OK" : "FAILED";
    }

    public CompletionStage<Result> apiLogon(Http.Request request) {

        val userPass = String.format("%s:%s", configuration.getString("auth.feedback.user"), configuration.getString("auth.feedback.password"));
        val required = Base64.getEncoder().encodeToString(userPass.getBytes());
        val supplied = request.header(AUTHORIZATION).orElse("").substring(6);

        return supplied.equals(required) ?
                offenderApi.logon(request.body().asText()).thenApply(Results::ok) :
                CompletableFuture.supplyAsync(Results::unauthorized);
    }

    private Map<String, Object> runtimeInfo() {

        val processors = getRuntime().availableProcessors();
        val systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100 / processors;

        return ImmutableMap.of(
                "processors", processors,
                "freeMemory", FileUtils.byteCountToDisplaySize(getRuntime().freeMemory()),
                "totalMemory", FileUtils.byteCountToDisplaySize(getRuntime().totalMemory()),
                "maxMemory", FileUtils.byteCountToDisplaySize(getRuntime().maxMemory()),
                "systemLoad", String.format("%.2f %%", systemLoad)
        );
    }

    private String localhost() {

        String localHost;

        try {
            localHost = InetAddress.getLocalHost().toString();

        } catch (UnknownHostException ex) {

            localHost = "unknown";
        }

        return localHost;
    }

    private Iterable<Map<String, String>> fileSystemDetails() {

        return Arrays.stream(File.listRoots()).map(root -> ImmutableMap.of(
                    "filePath", root.getAbsolutePath(),
                    "totalSpace", FileUtils.byteCountToDisplaySize(root.getTotalSpace()),
                    "freeSpace", FileUtils.byteCountToDisplaySize(root.getFreeSpace()),
                    "usableSpace", FileUtils.byteCountToDisplaySize(root.getUsableSpace())
        )).collect(Collectors.toList());
    }

    private String version() {

        return Optional.ofNullable(UtilityController.class.getPackage()).
                flatMap(pkg -> Optional.ofNullable(pkg.getImplementationVersion())).
                orElse("UNKNOWN");
    }
}
