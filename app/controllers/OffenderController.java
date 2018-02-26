package controllers;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import helpers.Encryption;
import helpers.JsonHelper;
import interfaces.PrisonerApi;
import lombok.val;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static helpers.JwtHelper.principal;

public class OffenderController extends Controller {

    private final PrisonerApi prisonerApi;
    private final HttpExecutionContext ec;
    private final Function<String, String> decrypter;

    @Inject
    public OffenderController(Config configuration, PrisonerApi prisonerApi, HttpExecutionContext ec) {

        this.ec = ec;
        this.prisonerApi = prisonerApi;

        val paramsSecretKey = configuration.getString("params.secret.key");

        decrypter = encrypted -> Encryption.decrypt(encrypted, paramsSecretKey);
    }

    public CompletionStage<Result> image(String oneTimeNomisRef) { // Can only be used by the user that generated a search result just now

        val reference = JsonHelper.jsonToMap(decrypter.apply(oneTimeNomisRef)); // Encrypted so cannot be changed from generation in oneTimeNomisRef()
                                                                                // The associated Offender has already been checked canAccess just now
        val validUser = Optional.ofNullable(reference.get("user")).
                map(user -> user.equals(principal(session("offenderApiBearerToken")))).     //@TODO: Shared constant
                orElse(false);
                                                                            // oneTimeNomisRef() creation in ElasticOffenderSearch results
        val validTick = Optional.ofNullable(reference.get("tick")).         // is synchronous and fast so does not affect Search performance.
                map(Long::valueOf).                                         // The slow asynchronous fetching of Nomis Images is orchestrated
                map(tick -> Instant.now().toEpochMilli() - tick).           // by the ReactJS front end after the Search results have been
                map(Math::abs).                                             // displayed in the browser inherently by setting image.src to
                map(difference -> difference < 60000).                      // the oneTimeNomisRef and displayed to users after results load
                orElse(false);
                                                            // Without the above would need to find associate Offender and call API canAccess again 
        return Optional.ofNullable(reference.get("noms")).
                filter(nomisId -> validUser && validTick && !Strings.isNullOrEmpty(nomisId)).
                map(nomisId -> prisonerApi.getImage(nomisId).thenApplyAsync(bytes -> ok(bytes).as("image/png"), ec.current())). //@TODO: check is png
                orElseGet(() -> {

                    Logger.warn("Invalid OneTimeNomisRef: {}", oneTimeNomisRef);
                    return CompletableFuture.completedFuture(unauthorized());
                });
    }
}

//@TODO: - in client - image.src='offender/oneTimeNomisRef/{ url-encoded one-time--ref }/image'
//@todo: use this in a JSON node of Nomis ids that exist in response from search - and alter front end to use
// need to set env vars, maybe NationlUser too
// -  to do - Decoing above here, Encoding in JSON elastic search response if a nomis id extts, use in Javascript src img, and defailt images too
// Then use in frtont en anf get some defaule female/male images
// Just need config in preProd and possibly the local dns of dnt-001/2 etc.
