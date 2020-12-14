package io.github.ititus.factorio.recipes;

import io.github.ititus.factorio.recipes.data.recipe.Mode;
import io.github.ititus.math.time.DurationFormatter;
import io.github.ititus.math.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Config {

    private static final boolean DEFAULT_VERBOSE = false;
    private static final boolean DEFAULT_OFFLINE_MODE = false;
    private static final boolean DEFAULT_DUMP_LUA_GLOBALS = false;
    private static final String DEFAULT_MODE = Mode.NORMAL.getName();
    private static final JSONArray DEFAULT_RECIPE_WHITELIST = new JSONArray(List.of(
            "solid-fuel-from-light-oil"
    ));
    private static final JSONArray DEFAULT_BASE_ITEMS = new JSONArray(List.of(
            "copper-plate", "iron-plate", "steel-plate",
            "coal",
            "stone", "stone-brick",
            "water",
            "heavy-oil", "light-oil", "petroleum-gas",
            "sulfuric-acid"
    ));
    private static final JSONObject DEFAULT_CRAFTING_MACHINES = new JSONObject(Map.of(
            "advanced-crafting", "assembling-machine-3",
            "basic-crafting", "assembling-machine-3",
            "centrifuging-crafting", "centrifuging",
            "chemistry", "chemical-plant",
            "crafting", "assembling-machine-3",
            "crafting-with-fluid", "assembling-machine-3",
            "oil-processing", "oil-refinery",
            "rocket-building", "rocket-silo",
            "smelting", "electric-furnace"
    ));
    private static final String DEFAULT_OUTPUT_DIR = "output";
    private static final String DEFAULT_DOT_DIR = DEFAULT_OUTPUT_DIR + "/dot";
    private static final String DEFAULT_FACTORIO_DATA_DIR = DEFAULT_OUTPUT_DIR + "/factorio-data";
    private static final String DEFAULT_GRAPHVIZ_DIR = "C:/Program Files/Graphviz 2.41.20190326.1719/bin";

    public static boolean verbose;
    public static boolean offlineMode;
    public static boolean dumpLuaGlobals;
    public static Mode mode;
    public static Set<String> recipeWhitelist;
    public static Set<String> baseItems;
    public static Map<String, String> craftingMachines;
    public static Path outputDir;
    public static Path dotDir;
    public static Path factorioDataDir;
    public static Path graphvizDir;

    public static void load(Path configFile) {
        StopWatch s = StopWatch.createRunning();

        JSONObject in;
        JSONObject out = new JSONObject();

        if (Files.isRegularFile(configFile)) {
            try (Reader r = Files.newBufferedReader(configFile)) {
                in = new JSONObject(new JSONTokener(r));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            in = null;
        }

        verbose = get(in, out, "verbose", DEFAULT_VERBOSE);
        offlineMode = get(in, out, "offlineMode", DEFAULT_OFFLINE_MODE);
        dumpLuaGlobals = get(in, out, "dumpLuaGlobals", DEFAULT_DUMP_LUA_GLOBALS);
        mode = Mode.get(get(in, out, "mode", DEFAULT_MODE));

        recipeWhitelist = toStringSet(get(in, out, "recipeWhitelist", DEFAULT_RECIPE_WHITELIST));

        baseItems = toStringSet(get(in, out, "baseItems", DEFAULT_BASE_ITEMS));

        craftingMachines = get(in, out, "craftingMachines", DEFAULT_CRAFTING_MACHINES).toMap().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().toString()));

        outputDir = Path.of(get(in, out, "outputDir", DEFAULT_OUTPUT_DIR)).normalize();
        dotDir = Path.of(get(in, out, "dotDir", DEFAULT_DOT_DIR)).normalize();
        factorioDataDir = Path.of(get(in, out, "factorioDataDir", DEFAULT_FACTORIO_DATA_DIR)).normalize();
        graphvizDir = Path.of(get(in, out, "graphvizDir", DEFAULT_GRAPHVIZ_DIR)).normalize();

        try {
            Files.createDirectories(Config.outputDir);
            Files.createDirectories(Config.dotDir);
            Files.createDirectories(Config.factorioDataDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (Writer w = Files.newBufferedWriter(configFile)) {
            out.write(w, 4, 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        System.out.println("Loaded config. Time elapsed: " + DurationFormatter.formatMillis(s.stop()));
    }

    private static boolean get(JSONObject in, JSONObject out, String key, boolean default_) {
        boolean b;
        if (in != null && in.has(key)) {
            out.put(key, b = in.getBoolean(key));
        } else {
            out.put(key, b = default_);
        }
        return b;
    }

    private static String get(JSONObject in, JSONObject out, String key, String default_) {
        String s;
        if (in != null && in.has(key)) {
            out.put(key, s = in.getString(key));
        } else {
            out.put(key, s = default_);
        }
        return s;
    }

    private static JSONArray get(JSONObject in, JSONObject out, String key, JSONArray default_) {
        JSONArray a;
        if (in != null && in.has(key)) {
            out.put(key, a = in.getJSONArray(key));
        } else {
            out.put(key, a = default_);
        }
        return a;
    }

    private static JSONObject get(JSONObject in, JSONObject out, String key, JSONObject default_) {
        JSONObject o;
        if (in != null && in.has(key)) {
            out.put(key, o = in.getJSONObject(key));
        } else {
            out.put(key, o = default_);
        }
        return o;
    }

    private static Set<String> toStringSet(JSONArray a) {
        return stream(a, a.length()).map(Object::toString).collect(Collectors.toUnmodifiableSet());
    }

    private static <T> Stream<T> stream(Iterable<T> iterable, int size) {
        return StreamSupport.stream(Spliterators.spliterator(iterable.iterator(), size,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }
}
