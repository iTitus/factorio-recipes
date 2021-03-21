package io.github.ititus.factorio.recipes.data.recipe;

import io.github.ititus.data.Printable;
import io.github.ititus.factorio.recipes.FactorioRecipes;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.math.number.BigRational;
import io.github.ititus.math.number.BigRationalConstants;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Map;
import java.util.Objects;

public final class Product implements Printable {

    private final String type, name;
    private final int amount, amountMin, amountMax;
    private final BigRational probability;

    // Not relevant: temperature

    public Product(LuaTable t) {
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

        if (named) {
            this.probability = LuaUtil.getBigRational(t, "probability", BigRationalConstants.ONE);
            this.amountMin = LuaUtil.getInt(t, "amount_min", this.amount);
            this.amountMax = LuaUtil.getInt(t, "amount_max", this.amount);
        } else {
            this.probability = BigRationalConstants.ONE;
            if (!t.rawget("probability").isnil()) {
                throw new RuntimeException();
            }
            this.amountMin = this.amountMax = this.amount;
            if (!t.rawget("amount_min").isnil() || !t.rawget("amount_max").isnil()) {
                throw new RuntimeException();
            }
        }
    }

    public Product(String name, int amount) {
        this.type = FactorioRecipes.ITEM;
        this.name = name;
        this.amount = this.amountMin = this.amountMax = amount;
        this.probability = BigRationalConstants.ONE;
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

    public boolean isComplex() {
        return !BigRationalConstants.ONE.equals(probability) || amountMin != amount || amountMax != amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return amount == product.amount && amountMin == product.amountMin && amountMax == product.amountMax && Objects.equals(type, product.type) && Objects.equals(name, product.name) && Objects.equals(probability, product.probability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, amount, amountMin, amountMax, probability);
    }

    @Override
    public String getPrefix() {
        return "product";
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        fields.put("type", type);
        fields.put("name", name);
        fields.put("amount", String.valueOf(amount));
        if (isComplex()) {
            fields.put("amountMin", String.valueOf(amountMin));
            fields.put("amountMax", String.valueOf(amountMax));
            fields.put("probability", String.valueOf(probability));
        }
    }

    @Override
    public String toString() {
        return toPrintableString();
    }
}
