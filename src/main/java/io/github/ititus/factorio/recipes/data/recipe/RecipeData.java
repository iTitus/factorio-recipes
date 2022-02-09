package io.github.ititus.factorio.recipes.data.recipe;

import io.github.ititus.commons.data.Printable;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.commons.math.number.BigRational;
import io.github.ititus.commons.math.number.BigRationalConstants;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RecipeData implements Printable {

    private final List<Ingredient> ingredients;
    private final List<Product> results;
    private final BigRational time;
    private final boolean allowDecomposition;

    // requester_paste_multiplier (int[def=30, but this can be changed in game options])
    // Not found: emissions_multiplier, overload_multiplier

    // Not relevant: enabled, hidden
    // Not found: hide_from_stats, allow_as_intermediate, allow_intermediates, always_show_made_in, show_amount_in_title

    // main_product (Optional<String> with a twist)

    public RecipeData(LuaTable t) {
        this.ingredients = LuaUtil.getList(t, "ingredients", Ingredient::new);
        if (this.ingredients.isEmpty()) {
            throw new RuntimeException();
        }

        LuaValue result = t.rawget("result");
        LuaValue results = t.rawget("results");
        if (result.isnil() && results.istable()) {
            this.results = LuaUtil.getList(t, "results", Product::new);
        } else if (result.isstring() && results.isnil()) {
            this.results = List.of(new Product(result.checkjstring(), LuaUtil.getInt(t, "result_count", 1)));
        } else {
            throw new RuntimeException();
        }
        if (this.results.isEmpty()) {
            throw new RuntimeException();
        }

        this.time = LuaUtil.getBigRational(t, "energy_required", BigRationalConstants.ONE_OVER_TWO);
        this.allowDecomposition = LuaUtil.getBoolean(t, "allow_decomposition", true);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Product> getResults() {
        return results;
    }

    public BigRational getTime() {
        return time;
    }

    public boolean allowDecomposition() {
        return allowDecomposition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecipeData that = (RecipeData) o;
        return allowDecomposition == that.allowDecomposition && Objects.equals(ingredients, that.ingredients) && Objects.equals(results, that.results) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredients, results, time, allowDecomposition);
    }

    @Override
    public String getPrefix() {
        return "recipe-data";
    }

    @Override
    public void getPrintableFields(Map<String, Object> fields) {
        fields.put("ingredients", ingredients);
        fields.put("results", results);
        fields.put("time", time);
        fields.put("allowDecomposition", allowDecomposition);
    }

    @Override
    public String toString() {
        return toPrintableString();
    }
}
