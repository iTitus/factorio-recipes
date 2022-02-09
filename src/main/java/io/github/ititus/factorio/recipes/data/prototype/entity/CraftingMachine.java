package io.github.ititus.factorio.recipes.data.prototype.entity;

import io.github.ititus.factorio.recipes.data.item.module.ModuleEffect;
import io.github.ititus.factorio.recipes.data.item.module.ModuleSpecification;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.commons.math.number.BigRational;
import io.github.ititus.commons.math.number.BigRationalConstants;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class CraftingMachine extends EntityWithHealth {

    protected final BigRational craftingSpeed, baseProductivity;
    protected final Set<String> recipeCategories, allowedEffects;
    protected final ModuleSpecification moduleSpecification;

    public CraftingMachine(LuaTable t) {
        super(t);
        this.craftingSpeed = LuaUtil.getBigRational(t, "crafting_speed");
        this.baseProductivity = LuaUtil.getBigRational(t, "base_productivity", BigRationalConstants.ZERO);
        this.recipeCategories = LuaUtil.getStringSet(t, "crafting_categories");
        this.allowedEffects = LuaUtil.getStringSet(t, "allowed_effects");
        this.moduleSpecification = new ModuleSpecification(LuaUtil.getTableOrEmpty(t, "module_specification"));
    }

    public BigRational getCraftingSpeed() {
        return craftingSpeed;
    }

    public BigRational getBaseProductivity() {
        return baseProductivity;
    }

    public Set<String> getRecipeCategories() {
        return recipeCategories;
    }

    public Set<String> getAllowedEffects() {
        return allowedEffects;
    }

    public ModuleSpecification getModuleSpecification() {
        return moduleSpecification;
    }

    public ModuleEffect filter(ModuleEffect effect) {
        ModuleEffect.Builder b = ModuleEffect.builder();
        if (allowedEffects.contains("consumption")) {
            b.consumption(effect.getConsumptionBonus());
        }
        if (allowedEffects.contains("productivity")) {
            b.productivity(effect.getProductivityBonus());
        }
        if (allowedEffects.contains("pollution")) {
            b.pollution(effect.getPollutionBonus());
        }
        if (allowedEffects.contains("speed")) {
            b.speed(effect.getSpeedBonus());
        }
        return b.build();
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
        CraftingMachine that = (CraftingMachine) o;
        return Objects.equals(craftingSpeed, that.craftingSpeed) && Objects.equals(baseProductivity,
                that.baseProductivity) && Objects.equals(recipeCategories, that.recipeCategories) && Objects.equals(allowedEffects, that.allowedEffects) && Objects.equals(moduleSpecification, that.moduleSpecification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), craftingSpeed, baseProductivity, recipeCategories, allowedEffects,
                moduleSpecification);
    }

    @Override
    public void getPrintableFields(Map<String, Object> fields) {
        super.getPrintableFields(fields);
        fields.put("craftingSpeed", craftingSpeed);
        fields.put("baseProductivity", baseProductivity);
        fields.put("recipeCategories", recipeCategories);
        fields.put("allowedEffects", allowedEffects);
        fields.put("moduleSpecification", moduleSpecification);
    }
}
