package io.github.ititus.factorio.recipes.data.recipe;

import io.github.ititus.data.Printable;
import io.github.ititus.factorio.recipes.FactorioRecipes;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Map;
import java.util.Objects;

public final class Ingredient implements Printable {

    private final String type, name;
    private final int amount;

    // Not found/irrelevant: temperature, minimum_temperature, maximum_temperature, catalyst_amount

    public Ingredient(LuaTable t) {
        this.type = LuaUtil.getString(t, "type", FactorioRecipes.ITEM);
        if (!Objects.equals(FactorioRecipes.ITEM, this.type) && !Objects.equals(FactorioRecipes.FLUID, this.type)) {
            throw new RuntimeException();
        }

        LuaValue namedName = t.rawget("name");
        LuaValue numberedName = t.rawget(1);
        boolean named = numberedName.isnil();
        if (named == namedName.isnil()) {
            throw new RuntimeException();
        }
        if (named) {
            this.name = namedName.checkjstring();
        } else {
            this.name = numberedName.checkjstring();
        }

        LuaValue namedAmount = t.rawget("amount");
        LuaValue numberedAmount = t.rawget(2);
        if (namedAmount.isnil() == numberedAmount.isnil()) {
            throw new RuntimeException();
        }
        if (named) {
            this.amount = namedAmount.checkint();
            if (namedAmount.isnumber() && !namedAmount.islong()) {
                throw new RuntimeException(namedAmount + " is a floating point number");
            }
        } else {
            this.amount = numberedAmount.checkint();
            if (numberedAmount.isnumber() && !numberedAmount.islong()) {
                throw new RuntimeException(numberedAmount + " is a floating point number");
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ingredient that = (Ingredient) o;
        return amount == that.amount && Objects.equals(type, that.type) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, amount);
    }

    @Override
    public String getPrefix() {
        return "ingredient";
    }

    @Override
    public void getPrintableFields(Map<String, Object> fields) {
        fields.put("type", type);
        fields.put("name", name);
        fields.put("amount", amount);
    }

    @Override
    public String toString() {
        return toPrintableString();
    }
}
