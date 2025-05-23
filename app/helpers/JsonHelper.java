package helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import static play.mvc.Http.Status.SERVICE_UNAVAILABLE;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static play.mvc.Results.status;

public interface JsonHelper {

    static Map<String, String> jsonToMap(String json) {

        return jsonToMap(Json.parse(json));
    }

    static Map<String, Object> jsonToObjectMap(String json) {

        return jsonToObjectMap(Json.parse(json));
    }

    static Map<String, String> jsonToMap(JsonNode json) {

        val mapper = Json.mapper();

        try {
            return mapper.readValue(
                    mapper.treeAsTokens(json),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
            );
        } catch (IOException ex) {
            Logger.error("Unable to parse json to Map<String, String>. " + json.toString(), ex);
            return null;
        }
    }

    static Map<String, Object> jsonToObjectMap(JsonNode json) {

        val mapper = Json.mapper();

        try {
            return mapper.readValue(
                    mapper.treeAsTokens(json),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );
        } catch (IOException ex) {
            Logger.error("Unable to parse json to Map<String, Object>. " + json.toString(), ex);
            return null;
        }
    }

    static <T> Result okJson(T data) {

        return ok(Json.toJson(data));
    }

    static <T> Result serverUnavailableJson(T data) {
        return status(SERVICE_UNAVAILABLE, Json.toJson(data));
    }

    static <T> Result badRequestJson(T data) {
        return badRequest(Json.toJson(data));
    }

    static <T> String stringify(T data) {

        return Json.stringify(Json.toJson(data));
    }

    static <T> T readValue(String json, Class<T> clazz) {

        return Json.fromJson(Json.parse(json), clazz);
    }

    static <T> T readValue(String json, TypeReference<T> type) {
        try {
            return Json.mapper().readValue(json, type);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Boolean toBoolean(ObjectNode rootNode, String nodeName) {

        return Optional.ofNullable(rootNode.get(nodeName)).map(JsonNode::asBoolean).orElse(false);
    }
}
