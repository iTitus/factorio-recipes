package io.github.ititus.factorio.recipes.data.prototype.entity;

import io.github.ititus.factorio.recipes.data.prototype.Prototype;
import org.luaj.vm2.LuaTable;

public abstract class Entity extends Prototype {

    public Entity(LuaTable t) {
        super(t);
    }
}
