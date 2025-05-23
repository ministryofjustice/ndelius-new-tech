package filters;

import com.github.coveo.ua_parser.Parser;
import com.google.common.collect.ImmutableList;
import helpers.JwtHelper;
import lombok.val;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import scala.collection.JavaConverters;

import java.util.List;
import java.util.Optional;

import static controllers.SessionKeys.OFFENDER_API_BEARER_TOKEN;
import static play.mvc.Http.HeaderNames.USER_AGENT;
import static play.mvc.Http.MimeTypes.HTML;
import static play.mvc.Http.MimeTypes.JSON;

class RequestLogger {
    private static List<String> loggableContent = ImmutableList.of(HTML, JSON, "application/pdf");

    static Optional<String> requestLogLine(long startTime, Http.RequestHeader requestHeader, Result result) {
        val session = session(requestHeader);

        val endTime = System.currentTimeMillis();
        val requestTime = endTime - startTime;

        return result.contentType()
                .filter(RequestLogger::shouldLog)
                .map(notUsed ->
                        String.format("%s; %s; '%s'; %s; %s; %s; %dms",
                                id(session),
                                userId(session),
                                browser(requestHeader),
                                requestHeader.method(),
                                requestHeader.uri(),
                                result.status(),
                                requestTime));

    }
    private static boolean shouldLog(String contentType) {
        return loggableContent.contains(contentType);
    }

    private static Http.Session session(Http.RequestHeader requestHeader) {
        return new Http.Session(JavaConverters.mapAsJavaMap(requestHeader.asScala().session().data()));
    }

    private static String userId(Http.Session session) {
        return session.get(OFFENDER_API_BEARER_TOKEN)
                .map(JwtHelper::principal)
                .orElse("unknown user");
    }

    private static String id(Http.Session session) {
        return session.get("id").orElse("unknown id");
    }

    private static String browser(Http.RequestHeader requestHeader) {
        return requestHeader.header(USER_AGENT).map(userAgent -> {
            try {
                val client = new Parser().parse(userAgent);
                return String.format("%s %s", client.userAgent.family, client.userAgent.major);
            } catch (Exception e) {
                Logger.warn("Unable to parse user agent", e);
                return "unknown browser - error";
            }

        }).orElse("unknown browser");
    }

}
