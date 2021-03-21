package io.github.ititus.factorio.recipes.data.prototype;

import io.github.ititus.data.Printable;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Objects;

public abstract class Prototype implements Comparable<Prototype>, Printable {

    // Not found / irrelevant: localised_name, localised_description

    protected final String type, name, order;

    public Prototype(LuaTable t) {
        this.type = LuaUtil.getString(t, "type");
        this.name = LuaUtil.getString(t, "name");
        this.order = LuaUtil.getString(t, "order", this.name);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public int compareTo(Prototype o) {
        return order.compareTo(o.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Prototype prototype = (Prototype) o;
        return Objects.equals(type, prototype.type) && Objects.equals(name, prototype.name) && Objects.equals(order,
                prototype.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, order);
    }

    @Override
    public final String getPrefix() {
        return type;
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        // type is the prefix
        fields.put("name", name);
        fields.put("order", order);
    }

    @Override
    public String toString() {
        return toPrintableString();
    }
}
