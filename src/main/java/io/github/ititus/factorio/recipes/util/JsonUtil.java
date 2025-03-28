package io.github.ititus.factorio.recipes.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static JSONObject loadJSONFromURL(String url) {
        JSONObject obj;
        try (InputStreamReader r = new InputStreamReader(new URI(url).toURL().openStream(), StandardCharsets.UTF_8)) {
            obj = new JSONObject(new JSONTokener(r));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

}
