package io.github.ititus.factorio.recipes.data.prototype.recipe;

import io.github.ititus.factorio.recipes.data.prototype.Prototype;
import io.github.ititus.factorio.recipes.data.recipe.Ingredient;
import io.github.ititus.factorio.recipes.data.recipe.Mode;
import io.github.ititus.factorio.recipes.data.recipe.Product;
import io.github.ititus.factorio.recipes.data.recipe.RecipeData;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.math.number.BigRational;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Recipe extends Prototype {

    private final String category;
    // Not relevant: subgroup, icons, icon, icon_size, crafting_machine_tint

    private final RecipeData normal, expensive;

    public Recipe(LuaTable t) {
        super(t);

        this.category = LuaUtil.getString(t, "category", "crafting");

        LuaValue normal = t.rawget("normal");
        LuaValue expensive = t.rawget("expensive");
        if (normal.isnil() && expensive.isnil()) {
            this.normal = this.expensive = new RecipeData(t);
        } else if (normal.istable() && expensive.istable()) {
            this.normal = new RecipeData(normal.checktable());
            this.expensive = new RecipeData(expensive.checktable());
        } else if (normal.istable() && expensive.toboolean()) {
            this.normal = this.expensive = new RecipeData(normal.checktable());
        } else if (normal.toboolean() && expensive.istable()) {
            this.normal = this.expensive = new RecipeData(expensive.checktable());
        } else {
            throw new RuntimeException();
        }
    }

    public String getCategory() {
        return category;
    }

    private RecipeData get(Mode mode) {
        if (mode == Mode.EXPENSIVE) {
            return expensive;
        }
        return normal;
    }

    public List<Ingredient> getIngredients(Mode mode) {
        return get(mode).getIngredients();
    }

    public List<Product> getResults(Mode mode) {
        return get(mode).getResults();
    }

    public BigRational getTime(Mode mode) {
        return get(mode).getTime();
    }

    public boolean allowDecomposition(Mode mode) {
        return get(mode).allowDecomposition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Recipe recipe = (Recipe) o;
        return Objects.equals(category, recipe.category) && Objects.equals(normal, recipe.normal) && Objects.equals(expensive, recipe.expensive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), category, normal, expensive);
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        super.getPrintableFields(fields);
        fields.put("category", category);
        if (normal == expensive) {
            fields.put("recipe", normal.toString());
        } else {
            fields.put("normal", normal.toString());
            fields.put("expensive", expensive.toString());
        }
    }
}
