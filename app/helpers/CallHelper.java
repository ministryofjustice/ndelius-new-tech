package helpers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import play.api.mvc.Call;
import play.mvc.Http;

public interface CallHelper {

    static String relative(Http.Request request, String url) {

        String path = request.path();
        int depth = Math.max(0, StringUtils.countMatches(path, "/") - 1);
        String relative = String.join("", IntStream.range(0, depth).mapToObj(x -> "../").collect(Collectors.toList()));

        return relative + url.substring(1);
    }

    static Call relative(Http.Request request, Call call) {

        return call.copy(call.method(), relative(request, call.url()), call.fragment());
    }
}
