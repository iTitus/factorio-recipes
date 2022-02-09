package io.github.ititus.factorio.recipes.data;

import io.github.ititus.factorio.recipes.Config;
import io.github.ititus.factorio.recipes.data.prototype.Prototype;
import io.github.ititus.factorio.recipes.data.prototype.PrototypeSet;
import io.github.ititus.factorio.recipes.data.prototype.category.ModuleCategory;
import io.github.ititus.factorio.recipes.data.prototype.category.RecipeCategory;
import io.github.ititus.factorio.recipes.data.prototype.entity.*;
import io.github.ititus.factorio.recipes.data.prototype.fluid.Fluid;
import io.github.ititus.factorio.recipes.data.prototype.item.Item;
import io.github.ititus.factorio.recipes.data.prototype.item.Module;
import io.github.ititus.factorio.recipes.data.prototype.recipe.Recipe;
import io.github.ititus.factorio.recipes.util.JsonUtil;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.factorio.recipes.util.task.DownloadGitBlobSHA1Task;
import io.github.ititus.commons.math.time.DurationFormatter;
import io.github.ititus.commons.math.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FactorioData {

    private static final String FACTORIO_DATA_QUERY_URL =
            "https://api.github.com/repos/wube/factorio-data/git/trees/master?recursive=1";
    private static final String FACTORIO_DATA_DOWNLOAD_URL =
            "https://raw.githubusercontent.com/wube/factorio-data/master/";
    private static final String SERPENT_QUERY_URL =
            "https://api.github.com/repos/pkulchenko/serpent/git/trees/master?recursive=1";
    private static final String SERPENT_DOWNLOAD_URL =
            "https://raw.githubusercontent.com/pkulchenko/serpent/master/src/serpent.lua";

    private final List<PrototypeSet<? extends Prototype>> prototypeSets = new ArrayList<>();

    private final PrototypeSet<Entity> entities;
    private final PrototypeSet<EntityWithHealth> entitiesWithHealth;
    private final PrototypeSet<Lab> labs;
    private final PrototypeSet<CraftingMachine> craftingMachines;
    private final PrototypeSet<Furnace> furnaces;
    private final PrototypeSet<AssemblingMachine> assemblingMachines;
    private final PrototypeSet<RocketSilo> rocketSilos;

    private final PrototypeSet<Item> items;
    private final PrototypeSet<ModuleCategory> moduleCategories;
    private final PrototypeSet<Module> modules;

    private final PrototypeSet<Fluid> fluids;

    private final PrototypeSet<RecipeCategory> recipeCategories;
    private final PrototypeSet<Recipe> recipes;

    public FactorioData(LuaTable t) {
        StopWatch s = StopWatch.createRunning();

        this.entities = pSet(Entity.class);
        this.entitiesWithHealth = pSet(EntityWithHealth.class);
        this.labs = pSet(Lab.class);
        this.craftingMachines = pSet(CraftingMachine.class);
        this.furnaces = pSet(Furnace.class);
        this.assemblingMachines = pSet(AssemblingMachine.class);
        this.rocketSilos = pSet(RocketSilo.class);

        this.items = pSet(Item.class);
        this.moduleCategories = pSet(ModuleCategory.class);
        this.modules = pSet(Module.class);

        this.fluids = pSet(Fluid.class);

        this.recipeCategories = pSet(RecipeCategory.class);
        this.recipes = pSet(Recipe.class);

        registerAll(t, "lab", Lab::new);
        registerAll(t, "assembling-machine", AssemblingMachine::new);
        registerAll(t, "rocket-silo", RocketSilo::new);
        registerAll(t, "furnace", Furnace::new);

        registerAll(t, "module-category", ModuleCategory::new);
        registerAll(t, "module", Module::new);

        registerAll(t, "fluid", Fluid::new);

        registerAll(t, "recipe-category", RecipeCategory::new);
        registerAll(t, "recipe", Recipe::new);

        this.prototypeSets.forEach(PrototypeSet::lock);

        System.out.println("Parsed lua data. Time elapsed: " + DurationFormatter.formatMillis(s.stop()));
        System.out.println();
    }

    public static FactorioData loadFactorioData() {
        StopWatch s = StopWatch.createRunning();

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Void>> jobs = new ArrayList<>();

        if (Config.offlineMode) {
            try (Stream<Path> walk = Files.walk(Config.factorioDataDir)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> !p.toString().endsWith(".sha1"))
                        .map(f -> new DownloadGitBlobSHA1Task(f, null, null, true))
                        .forEach(jobs::add);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            JSONArray serpentData = JsonUtil.loadJSONFromURL(SERPENT_QUERY_URL).getJSONArray("tree");
            for (int i = 0; i < serpentData.length(); i++) {
                JSONObject serpentObject = serpentData.getJSONObject(i);
                if (serpentObject.getString("path").equals("src/serpent.lua")) {
                    jobs.add(new DownloadGitBlobSHA1Task(Config.factorioDataDir.resolve("serpent.lua"),
                            serpentObject.getString("sha"), SERPENT_DOWNLOAD_URL, false));
                    break;
                }
            }

            JSONObject masterTree = JsonUtil.loadJSONFromURL(FACTORIO_DATA_QUERY_URL);
            if (masterTree.getBoolean("truncated")) {
                throw new RuntimeException("truncated");
            }

            JSONArray treeArray = masterTree.getJSONArray("tree");
            for (int i = 0; i < treeArray.length(); i++) {
                JSONObject obj = treeArray.getJSONObject(i);

                if (Objects.equals(obj.getString("type"), "blob")) {
                    String path = obj.getString("path");
                    jobs.add(new DownloadGitBlobSHA1Task(Config.factorioDataDir.resolve(path), obj.getString("sha"),
                            FACTORIO_DATA_DOWNLOAD_URL + path, false));
                }
            }
        }

        List<Future<Void>> futures;
        try {
            futures = executor.invokeAll(jobs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Checked/Downloaded lua files. Time elapsed: " + DurationFormatter.formatMillis(s.stop()));
        System.out.println();

        s.start();

        Globals globals = JsePlatform.debugGlobals();
        // LuaJC.install(globals);

        LuaFunction require = globals.get("require").checkfunction();

        String FILE_SEP = System.getProperty("file.separator");
        // copied from LuaJ
        // changed: resolve __mod-name__ to mod-name
        globals.get("package").set("searchpath", new VarArgFunction() {
            public Varargs invoke(Varargs va) {
                String moduleToSearch = va.checkjstring(1);
                String luaPath = va.checkjstring(2);
                char luaPathSeparator = va.optjstring(3, ".").charAt(0);
                char filePathSeparator = va.optjstring(4, FILE_SEP).charAt(0);

                moduleToSearch = moduleToSearch.replace(luaPathSeparator, filePathSeparator);

                // resolve mod dependencies, factorio data dir should be in lua path
                moduleToSearch = moduleToSearch.replaceAll("__([0-9A-Za-z-_]+)__", "$1");

                StringBuilder b = new StringBuilder();
                int nextSeparator = -1;
                do {
                    int start = nextSeparator + 1;
                    nextSeparator = luaPath.indexOf(';', start);
                    if (nextSeparator < 0) {
                        nextSeparator = luaPath.length();
                    }

                    String currentPath = luaPath.substring(start, nextSeparator);

                    int replaceIndex = currentPath.indexOf('?');
                    if (replaceIndex < 0) {
                        throw new IllegalArgumentException();
                    }

                    currentPath = currentPath.substring(0, replaceIndex)
                            + moduleToSearch + currentPath.substring(replaceIndex + 1);

                    InputStream is = globals.finder.findResource(currentPath);
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignored) {
                        }

                        return valueOf(currentPath);
                    }

                    b.append("\n\t").append(currentPath);
                } while (nextSeparator < luaPath.length());

                return varargsOf(NIL, valueOf(b.toString()));
            }
        });

        String factorioDataDir;
        try {
            factorioDataDir = Config.factorioDataDir.toRealPath().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        globals.package_.setLuaPath(
                PackageLib.DEFAULT_LUA_PATH + ";"
                        + factorioDataDir + "/?.lua"
        );
        require.call("serpent");
        globals.package_.setLuaPath(PackageLib.DEFAULT_LUA_PATH);

        require.call("defines");

        globals.package_.setLuaPath(
                PackageLib.DEFAULT_LUA_PATH + ";"
                        + factorioDataDir + "/core/lualib/?.lua"
        );
        require.call("dataloader");
        globals.package_.setLuaPath(PackageLib.DEFAULT_LUA_PATH);

        for (String fileName : new String[] { "data.lua", "data-updates.lua", "data-final-fixes.lua" }) {
            for (String modName : new String[] { "core", "base" }) {
                globals.package_.setLuaPath(
                        PackageLib.DEFAULT_LUA_PATH + ";"
                                + factorioDataDir + "/?.lua;"
                                + factorioDataDir + '/' + modName + "/?.lua;"
                                + factorioDataDir + "/core/lualib/?.lua"
                );

                Path p = Path.of(modName, fileName);
                if (Files.exists(Config.factorioDataDir.resolve(p))) {
                    LuaUtil.loadLuaFile(globals, Config.factorioDataDir, p.toString());
                }

                globals.package_.setLuaPath(PackageLib.DEFAULT_LUA_PATH);
            }
        }

        LuaTable rawData = LuaUtil.getTable(LuaUtil.getTable(globals, "data"), "raw");

        System.out.println("Executed lua files. Time elapsed: " + DurationFormatter.formatMillis(s.stop()));
        System.out.println();

        if (Config.dumpLuaGlobals) {
            StopWatch globalStopWatch = StopWatch.createRunning();
            String globalExport = globals.load("return serpent.block(_G, { nocode=true })").call().checkjstring();
            try {
                Files.writeString(Config.outputDir.resolve("globals.txt"), globalExport);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            System.out.println("Dumped globals. Time elapsed: " + DurationFormatter.formatMillis(globalStopWatch.stop()));
            System.out.println();
        }

        return new FactorioData(rawData);
    }

    public PrototypeSet<Entity> getEntities() {
        return entities;
    }

    public PrototypeSet<EntityWithHealth> getEntitiesWithHealth() {
        return entitiesWithHealth;
    }

    public PrototypeSet<Lab> getLabs() {
        return labs;
    }

    public PrototypeSet<CraftingMachine> getCraftingMachines() {
        return craftingMachines;
    }

    public PrototypeSet<Furnace> getFurnaces() {
        return furnaces;
    }

    public PrototypeSet<AssemblingMachine> getAssemblingMachines() {
        return assemblingMachines;
    }

    public PrototypeSet<RocketSilo> getRocketSilos() {
        return rocketSilos;
    }

    public PrototypeSet<Item> getItems() {
        return items;
    }

    public PrototypeSet<ModuleCategory> getModuleCategories() {
        return moduleCategories;
    }

    public PrototypeSet<Module> getModules() {
        return modules;
    }

    public PrototypeSet<Fluid> getFluids() {
        return fluids;
    }

    public PrototypeSet<RecipeCategory> getRecipeCategories() {
        return recipeCategories;
    }

    public PrototypeSet<Recipe> getRecipes() {
        return recipes;
    }

    private <T extends Prototype> PrototypeSet<T> pSet(Class<T> type) {
        PrototypeSet<T> ps = new PrototypeSet<>(type);
        prototypeSets.add(ps);
        return ps;
    }

    private void registerAll(LuaTable t, String category, Function<LuaTable, ? extends Prototype> fct) {
        LuaUtil.luaTableToList(LuaUtil.getTable(t, category), (k, v) -> {
                    Prototype p = fct.apply(v.checktable());
                    if (!k.checkjstring().equals(p.getName())) {
                        throw new RuntimeException(k + "=" + p);
                    }
                    return p;
                }
        ).forEach(this::registerPrototype);
    }

    private void registerPrototype(Prototype p) {
        prototypeSets.stream()
                .filter(ps -> ps.getType().isInstance(p))
                .forEach(ps -> ps.add(p));
    }
}
