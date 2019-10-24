package io.github.ititus.factorio.recipes.data.item.module;

import io.github.ititus.data.Printable;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Objects;

public final class ModuleSpecification implements Printable {

    private final int moduleSlots;

    public ModuleSpecification(LuaTable t) {
        this.moduleSlots = LuaUtil.getInt(t, "module_slots", 0);
    }

    public int getModuleSlots() {
        return moduleSlots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleSpecification that = (ModuleSpecification) o;
        return moduleSlots == that.moduleSlots;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleSlots);
    }

    @Override
    public String getPrefix() {
        return "module-specification";
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        fields.put("moduleSlots", String.valueOf(moduleSlots));
    }

    @Override
    public String toString() {
        return Printable.toPrintableString(this);
    }
}
