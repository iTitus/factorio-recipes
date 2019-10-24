package io.github.ititus.factorio.recipes;

import io.github.ititus.data.Pair;
import io.github.ititus.factorio.recipes.data.prototype.PrototypeSet;
import io.github.ititus.factorio.recipes.data.prototype.entity.CraftingMachine;
import io.github.ititus.factorio.recipes.data.prototype.recipe.Recipe;
import io.github.ititus.factorio.recipes.data.recipe.Ingredient;
import io.github.ititus.factorio.recipes.data.recipe.Mode;
import io.github.ititus.factorio.recipes.data.recipe.Product;
import io.github.ititus.math.hash.Hashing;
import io.github.ititus.math.number.BigRational;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeGraph {

    private final Mode mode;
    private final List<Recipe> recipes;
    private final Set<String> baseItems;
    private final Map<String, String> craftingMachinesUsed;
    private final PrototypeSet<CraftingMachine> craftingMachines;
    private final Map<String, Node> nodes;
    private final Node root;

    public RecipeGraph(Mode mode, List<Recipe> recipes, Set<String> baseItems, Map<String, String> craftingMachinesUsed, PrototypeSet<CraftingMachine> craftingMachines, String output) {
        this.mode = mode;
        this.recipes = List.copyOf(recipes);
        this.baseItems = Set.copyOf(baseItems);
        this.craftingMachinesUsed = Map.copyOf(craftingMachinesUsed);
        this.craftingMachines = craftingMachines;
        this.nodes = new HashMap<>();
        this.nodes.put(output, this.root = new Node(output));
        this.root.calculate(new HashSet<>());
    }

    public Node getRoot() {
        return root;
    }

    public void visit(Consumer<Node> visitor) {
        root.visit(new HashSet<>(), visitor);
    }

    public void findOptimalRatio() {
        List<String> lines = new ArrayList<>();
        List<String> baseNodes = new ArrayList<>();

        // Map<String, BigRational> amounts = new HashMap<>();
        // amounts.put(root.getName(), BigRationalConstants.ONE);

        lines.add("strict digraph " + root.getName().replace('-', '_') + " {");
        lines.add("    rankdir=BT");

        visit(n -> {
            StringBuilder b = new StringBuilder("    ").append(n.getName().replace('-', '_')).append(" [");

            boolean base = baseItems.contains(n.getName());
            if (base) {
                baseNodes.add(n.getName().replace("-", "_"));
            }

            if (base) {
                System.out.println("Visiting base node " + n.getName());
                b.append("label=\"").append(n.getName()).append("\" ").append("shape=ellipse color=gray fontcolor=gray");
            } else {
                System.out.println("Visiting node " + n.getProduct() + " with ingredients " + n.getRecipe().getIngredients(mode));
                BigRational time = n.getRecipe().getTime(mode);
                BigRational speed = getSpeed(n.getRecipe().getCategory());
                System.out.println("category=" + n.getRecipe().getCategory() + " rawTime=" + time + " speed=" + speed + " time=" + time.divide(speed));
                b.append("label=\"").append(n.getName()).append("\\n").append(n.getRecipe().getCategory()).append("\" ").append("shape=box color=black fontcolor=black");
            }

            lines.add(b.append("];").toString());

            if (!n.getProductNodes().isEmpty()) {
                boolean multi = n.getProductNodes().size() > 1;
                if (multi) {
                    System.out.println("Products: " + n.getProductNodes().stream().map(Pair::getA).map(Node::getProduct).map(Product::getName).collect(Collectors.toList()));
                } else {
                    System.out.println("Product: " + n.getProductNodes().get(0).getA().getProduct().getName());
                }

                for (Pair<Node, Ingredient> products : n.getProductNodes()) {
                    b.setLength(0);
                    b.append("    ").append(n.getName().replace('-', '_')).append(" -> ").append(products.getA().getName().replace('-', '_'));

                    if (multi) {
                        System.out.println("product=" + products.getA().getProduct().getName());
                    }
                    BigRational demand = BigRational.of(products.getB().getAmount()).divide(products.getA().getRecipe().getTime(mode).divide(getSpeed(products.getA().getRecipe().getCategory())));

                    b.append(" [");
                    if (base) {
                        System.out.println((multi ? "  " : "") + "demand=" + demand);
                        b.append("label=\"").append(demand.doubleValue()).append("\" color=gray fontcolor=gray");
                    } else {
                        BigRational supply = n.getProductProducedPerSecond();
                        BigRational ratio = supply.divide(demand);
                        System.out.println((multi ? "  " : "") + "demand=" + demand + " supply=" + supply);
                        System.out.println((multi ? "  " : "") + "ratio=" + ratio.toRatioString());
                        b.append("label=\"").append(ratio.inverse().toRatioString()).append("\" color=black fontcolor=black");
                    }

                    lines.add(b.append("];").toString());
                }
            }
            System.out.println();
        });

        StringBuilder b = new StringBuilder("    { rank=same;");
        baseNodes.forEach(n -> b.append(' ').append(n).append(';'));
        lines.add(b.append(" }").toString());

        lines.add("}");
        // lines.forEach(System.out::println);

        // amounts.entrySet().forEach(System.out::println);

        String[] toHash = new String[] {
                mode.getName(),
                recipes.toString(),
                baseItems.stream().sorted().collect(Collectors.joining()),
                craftingMachinesUsed.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Object::toString).collect(Collectors.joining()),
                craftingMachines.stream().flatMap(m -> Stream.<String>builder().add(m.getType()).add(m.getName()).add(m.getOrder()).add(m.getCraftingSpeed().toString()).build()).collect(Collectors.joining()),
                root.getName()
        };
        String hash = Hashing.MD_5.hash(toHash);
        Path dotFile = Config.dotDir.resolve(root.getName() + '_' + hash + ".dot").toAbsolutePath().normalize();
        try {
            Files.write(dotFile, lines);

            if (Files.isDirectory(Config.graphvizDir)) {
                int success = new ProcessBuilder()
                        .command(Config.graphvizDir.resolve("dot").toAbsolutePath().toString(), "-Tpng", "-o", root.getName() + '_' + hash + ".png", dotFile.toString())
                        .directory(Config.outputDir.toFile())
                        .inheritIO()
                        .start().waitFor();
                if (success != 0) {
                    throw new RuntimeException("Error while generating graph from dot file");
                }
            } else {
                System.out.println("Graphviz not found, only exported .dot file to " + dotFile + ". Install it from http://www.graphviz.org/");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Recipe findRecipe(String output) {
        List<Recipe> recipes = findRecipes(output);
        if (recipes.size() != 1) {
            throw new RuntimeException();
        }
        return recipes.get(0);
    }

    private List<Recipe> findRecipes(String output) {
        return recipes.stream().filter(r -> r.getResults(mode).stream().anyMatch(p -> Objects.equals(output, p.getName()))).collect(Collectors.toList());
    }

    private BigRational getSpeed(String recipeCategory) {
        return craftingMachines.get(craftingMachinesUsed.get(recipeCategory)).getCraftingSpeed();
    }

    public class Node {
        private final String name;
        private final List<Pair<Node, Ingredient>> productNodes;
        private final List<Node> ingredientNodes;
        private final Recipe recipe;
        private final Product product;
        private final BigRational productProducedPerSecond;

        private Node(String name) {
            this.name = name;
            if (baseItems.contains(name)) {
                this.recipe = null;
            } else {
                this.recipe = findRecipe(name);
                if (this.recipe.getResults(mode).size() != 1) {
                    throw new RuntimeException();
                }
            }
            this.productNodes = new ArrayList<>();
            this.ingredientNodes = new ArrayList<>();
            this.product = this.recipe != null ? this.recipe.getResults(mode).get(0) : null;
            this.productProducedPerSecond = this.recipe != null ? BigRational.of(this.product.getAmount()).divide(this.recipe.getTime(mode).divide(getSpeed(this.recipe.getCategory()))) : null;
        }

        public String getName() {
            return name;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public Product getProduct() {
            return product;
        }

        public BigRational getProductProducedPerSecond() {
            return productProducedPerSecond;
        }

        public List<Node> getIngredientNodes() {
            return ingredientNodes;
        }

        public List<Pair<Node, Ingredient>> getProductNodes() {
            return productNodes;
        }

        private void addProductNode(Pair<Node, Ingredient> parent) {
            this.productNodes.add(parent);
        }

        private void visit(Set<String> visited, Consumer<Node> visitor) {
            if (visited.add(name)) {
                visitor.accept(this);
                for (Node node : getIngredientNodes()) {
                    node.visit(visited, visitor);
                }
            }
        }

        private void calculate(Set<String> visited) {
            if (visited.add(name)) {
                if (recipe != null) {
                    for (Ingredient ingredient : recipe.getIngredients(mode)) {
                        Node n = nodes.computeIfAbsent(ingredient.getName(), Node::new);
                        n.addProductNode(Pair.of(this, ingredient));
                        ingredientNodes.add(n);
                    }
                    ingredientNodes.forEach(n -> n.calculate(visited));
                }
            }
        }
    }
}
