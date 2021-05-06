package io.github.ititus.factorio.recipes.data.prototype.entity;

import io.github.ititus.factorio.recipes.data.item.module.ModuleSpecification;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.math.number.BigRational;
import io.github.ititus.math.number.BigRationalConstants;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Lab extends EntityWithHealth {

    private final BigRational researchingSpeed, baseProductivity;
    private final Set<String> inputs;
    private final ModuleSpecification moduleSpecification;

    public Lab(LuaTable t) {
        super(t);
        this.researchingSpeed = LuaUtil.getBigRational(t, "researching_speed", BigRationalConstants.ONE);
        this.baseProductivity = LuaUtil.getBigRational(t, "base_productivity", BigRationalConstants.ZERO);
        this.inputs = LuaUtil.getStringSet(t, "inputs");
        this.moduleSpecification = new ModuleSpecification(LuaUtil.getTableOrEmpty(t, "module_specification"));
    }

    public BigRational getResearchingSpeed() {
        return researchingSpeed;
    }

    public BigRational getBaseProductivity() {
        return baseProductivity;
    }

    public Set<String> getInputs() {
        return inputs;
    }

    public ModuleSpecification getModuleSpecification() {
        return moduleSpecification;
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
        Lab that = (Lab) o;
        return Objects.equals(researchingSpeed, that.researchingSpeed) && Objects.equals(baseProductivity,
                that.baseProductivity) && Objects.equals(inputs, that.inputs) && Objects.equals(moduleSpecification,
                that.moduleSpecification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), researchingSpeed, baseProductivity, inputs, moduleSpecification);
    }

    @Override
    public void getPrintableFields(Map<String, Object> fields) {
        super.getPrintableFields(fields);
        fields.put("researchingSpeed", researchingSpeed);
        fields.put("baseProductivity", baseProductivity);
        fields.put("inputs", inputs);
        fields.put("moduleSpecification", moduleSpecification);
    }
}
