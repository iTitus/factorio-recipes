package io.github.ititus.factorio.recipes;

import io.github.ititus.factorio.recipes.data.FactorioData;
import io.github.ititus.factorio.recipes.data.prototype.Prototype;
import io.github.ititus.factorio.recipes.data.prototype.PrototypeSet;
import io.github.ititus.factorio.recipes.data.prototype.category.ModuleCategory;
import io.github.ititus.factorio.recipes.data.prototype.category.RecipeCategory;
import io.github.ititus.factorio.recipes.data.prototype.entity.CraftingMachine;
import io.github.ititus.factorio.recipes.data.prototype.item.Module;
import io.github.ititus.factorio.recipes.data.prototype.recipe.Recipe;
import io.github.ititus.factorio.recipes.data.recipe.Product;
import io.github.ititus.commons.math.time.DurationFormatter;
import io.github.ititus.commons.math.time.StopWatch;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FactorioRecipes {

    public static final String ITEM = "item";
    public static final String FLUID = "fluid";

    public static void main(String[] args) {
        System.out.println("FactorioRecipes: Starting...");
        if (args.length == 0) {
            System.out.println("No arguments given, calculating recipe ratios for some default items.");
        } else {
            System.out.println("Calculating recipe ratios for: " + Arrays.toString(args));
        }
        System.out.println();

        Config.load(Path.of("config.json"));
        System.out.println();

        FactorioData factorioData = FactorioData.loadFactorioData();
        PrototypeSet<RecipeCategory> recipeCategories = factorioData.getRecipeCategories();
        PrototypeSet<Recipe> recipes = factorioData.getRecipes();
        PrototypeSet<CraftingMachine> craftingMachines = factorioData.getCraftingMachines();
        PrototypeSet<ModuleCategory> moduleCategories = factorioData.getModuleCategories();
        PrototypeSet<Module> modules = factorioData.getModules();

        StopWatch s = StopWatch.createRunning();

        List<Recipe> tempRecipeList =
                recipes.stream().filter(r -> Config.recipeWhitelist.contains(r.getName()) || (r.allowDecomposition(Config.mode) && r.getResults(Config.mode).size() == 1 && !r.getResults(Config.mode).get(0).isComplex())).collect(Collectors.toUnmodifiableList());
        List<Recipe> removedComplex =
                recipes.stream().filter(r -> !tempRecipeList.contains(r)).collect(Collectors.toUnmodifiableList());

        List<String> duplicates = tempRecipeList.stream().filter(r -> {
            Product p = r.getResults(Config.mode).get(0);
            for (Recipe other : tempRecipeList) {
                if (r != other && p.getName().equals(other.getResults(Config.mode).get(0).getName())) {
                    return true;
                }
            }
            return false;
        }).map(Recipe::getName).filter(r -> !Config.recipeWhitelist.contains(r)).collect(Collectors.toUnmodifiableList());
        List<Recipe> finalRecipeList =
                tempRecipeList.stream().filter(r -> !duplicates.contains(r.getName())).sorted().collect(Collectors.toUnmodifiableList());

        System.out.println("Ignored " + removedComplex.size() + " recipes due to cycle problems or " +
                "multi/non-deterministic output:");
        System.out.println(removedComplex.stream().map(Recipe::getName).collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("Ignored " + duplicates.size() + " recipes due to duplicate outputs:");
        System.out.println(String.join(", ", duplicates));

        System.out.println();
        System.out.println("Filtered recipe list. Time elapsed: " + DurationFormatter.formatMillis(s.stop()));

        if (Config.verbose) {
            System.out.println();
            System.out.println("Recipe Categories:");
            for (RecipeCategory c : recipeCategories) {
                System.out.println(c.getName() + '=' + craftingMachines.stream().filter(m -> m.getRecipeCategories().contains(c.getName())).map(Prototype::getName).collect(Collectors.toList()));
            }
            System.out.println();
            System.out.println("Crafting Machines:");
            craftingMachines.forEach(System.out::println);
            System.out.println();
            System.out.println("Module Categories:");
            for (ModuleCategory c : moduleCategories) {
                System.out.println(c.getName() + '=' + modules.stream().filter(m -> Objects.equals(c.getName(),
                        m.getCategory())).map(Prototype::getName).collect(Collectors.toList()));
            }
            System.out.println();
            System.out.println("Modules:");
            modules.forEach(System.out::println);
            System.out.println();
            System.out.println("Recipes:");
            finalRecipeList.forEach(System.out::println);
        }

        System.out.println();
        s.start();

        if (args.length > 0) {
            for (String item : args) {
                System.out.println("#".repeat(80));
                RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                        Config.craftingMachines, craftingMachines, item);
                tree.findOptimalRatio();
            }
        } else {
            for (Recipe recipe : finalRecipeList) {
                if (!recipe.getName().startsWith("space-") && recipe.getName().endsWith("-science-pack")) {
                    System.out.println("#".repeat(80));
                    String outputItem = recipe.getName();
                    RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                            Config.craftingMachines, craftingMachines, outputItem);
                    tree.findOptimalRatio();
                } else if (recipe.getName().endsWith("-module-3")) {
                    System.out.println("#".repeat(80));
                    String outputItem = recipe.getName();
                    RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                            Config.craftingMachines, craftingMachines, outputItem);
                    tree.findOptimalRatio();
                } else if (recipe.getName().endsWith("-robot")) {
                    System.out.println("#".repeat(80));
                    String outputItem = recipe.getName();
                    RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                            Config.craftingMachines, craftingMachines, outputItem);
                    tree.findOptimalRatio();
                }
            }

            for (String misc : new String[] { "rail", "electric-engine-unit" }) {
                System.out.println("#".repeat(80));
                RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                        Config.craftingMachines, craftingMachines, misc);
                tree.findOptimalRatio();
            }

            for (String circuit : new String[] { "electronic-circuit", "advanced-circuit", "processing-unit" }) {
                System.out.println("#".repeat(80));
                RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                        Config.craftingMachines, craftingMachines, circuit);
                tree.findOptimalRatio();
            }

            for (String rocketPart : new String[] { "rocket-part", "rocket-control-unit", "low-density-structure",
                    "rocket-fuel" }) {
                System.out.println("#".repeat(80));
                RecipeGraph tree = new RecipeGraph(Config.mode, finalRecipeList, Config.baseItems,
                        Config.craftingMachines, craftingMachines, rocketPart);
                tree.findOptimalRatio();
            }
        }

        System.out.println("Created recipes tree(s). Time elapsed: " + DurationFormatter.formatMillis(s.stop()));
    }
}
