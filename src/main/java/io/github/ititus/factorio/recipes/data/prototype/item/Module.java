package io.github.ititus.factorio.recipes.data.prototype.item;

import io.github.ititus.factorio.recipes.data.item.module.ModuleEffect;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Module extends Item {

    private final String category;
    private final int tier;
    private final ModuleEffect effect;
    private final Set<String> limitation;

    // Not needed: limitation_message_key

    public Module(LuaTable t) {
        super(t);
        this.category = LuaUtil.getString(t, "category");
        this.tier = LuaUtil.getInt(t, "tier");
        this.effect = new ModuleEffect(LuaUtil.getTable(t, "effect"));
        this.limitation = LuaUtil.getStringSet(t, "limitation");
    }

    public boolean canBeUsedWith(String recipe) {
        return limitation.isEmpty() || limitation.contains(recipe);
    }

    public String getCategory() {
        return category;
    }

    public int getTier() {
        return tier;
    }

    public ModuleEffect getEffect() {
        return effect;
    }

    public Set<String> getLimitation() {
        return limitation;
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
        Module module = (Module) o;
        return tier == module.tier && Objects.equals(category, module.category) && Objects.equals(effect,
                module.effect) && Objects.equals(limitation, module.limitation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), category, tier, effect, limitation);
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        super.getPrintableFields(fields);
        fields.put("category", category);
        fields.put("tier", String.valueOf(tier));
        fields.put("effect", String.valueOf(effect));
        if (!limitation.isEmpty()) {
            fields.put("limitation", String.valueOf(limitation));
        }
    }
}
