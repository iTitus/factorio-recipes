package io.github.ititus.factorio.recipes.data.item.module;

import io.github.ititus.data.Printable;
import io.github.ititus.factorio.recipes.data.prototype.item.Module;
import io.github.ititus.factorio.recipes.util.LuaUtil;
import io.github.ititus.math.number.BigIntegerMath;
import io.github.ititus.math.number.BigRational;
import io.github.ititus.math.number.BigRationalConstants;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class ModuleEffect implements Printable {

    public static final ModuleEffect NO_EFFECT = new ModuleEffect(BigRationalConstants.ZERO,
            BigRationalConstants.ZERO, BigRationalConstants.ZERO, BigRationalConstants.ZERO);

    private final BigRational consumptionBonus, productivityBonus, pollutionBonus, speedBonus;

    public ModuleEffect(LuaTable t) {
        this.consumptionBonus = get(t, "consumption");
        this.productivityBonus = get(t, "productivity");
        this.pollutionBonus = get(t, "pollution");
        this.speedBonus = get(t, "speed");
    }

    private ModuleEffect(BigRational consumptionBonus, BigRational productivityBonus, BigRational pollutionBonus,
                         BigRational speedBonus) {
        this.consumptionBonus = consumptionBonus;
        this.productivityBonus = productivityBonus;
        this.pollutionBonus = pollutionBonus;
        this.speedBonus = speedBonus;
    }

    public static ModuleEffect of(Module... modules) {
        return Arrays.stream(modules).map(Module::getEffect).reduce(NO_EFFECT, ModuleEffect::add);
    }

    public static ModuleEffect of(Collection<Module> modules) {
        return modules.stream().map(Module::getEffect).reduce(NO_EFFECT, ModuleEffect::add);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static BigRational get(LuaTable t, String key) {
        LuaValue v1 = t.rawget(key);
        if (v1.isnil()) {
            return BigRationalConstants.ZERO;
        }

        return LuaUtil.getBigRational(v1.checktable(), "bonus");
    }

    public BigRational getConsumptionBonus() {
        return consumptionBonus;
    }

    public BigRational getProductivityBonus() {
        return productivityBonus;
    }

    public BigRational getPollutionBonus() {
        return pollutionBonus;
    }

    public BigRational getSpeedBonus() {
        return speedBonus;
    }

    public ModuleEffect add(ModuleEffect o) {
        if (o == NO_EFFECT) {
            return this;
        }
        return new ModuleEffect(consumptionBonus.add(o.consumptionBonus).max(BigRationalConstants.MINUS_FOUR_OVER_FIVE), productivityBonus.add(o.productivityBonus), pollutionBonus.add(o.pollutionBonus), speedBonus.add(o.speedBonus));
    }

    public ModuleEffect multiply(BigRational r) {
        if (r.isNegative()) {
            throw new IllegalArgumentException();
        } else if (r.isZero()) {
            return NO_EFFECT;
        }
        return new ModuleEffect(consumptionBonus.multiply(r).max(BigRationalConstants.MINUS_FOUR_OVER_FIVE),
                productivityBonus.multiply(r), pollutionBonus.multiply(r), speedBonus.multiply(r));
    }

    public ModuleEffect beaconized() {
        return beaconized(BigInteger.ONE);
    }

    public ModuleEffect beaconized(int beaconCount) {
        return beaconized(BigIntegerMath.of(beaconCount));
    }

    public ModuleEffect beaconized(BigInteger beaconCount) {
        //TODO: filter out productivity bonus
        return multiply(BigRational.of(beaconCount, BigInteger.TWO));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleEffect that = (ModuleEffect) o;
        return Objects.equals(consumptionBonus, that.consumptionBonus) && Objects.equals(productivityBonus,
                that.productivityBonus) && Objects.equals(pollutionBonus, that.pollutionBonus) && Objects.equals(speedBonus, that.speedBonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumptionBonus, productivityBonus, pollutionBonus, speedBonus);
    }

    @Override
    public String getPrefix() {
        return "effect";
    }

    @Override
    public void getPrintableFields(Map<String, String> fields) {
        if (!BigRationalConstants.ZERO.equals(consumptionBonus)) {
            fields.put("consumptionBonus", String.valueOf(consumptionBonus));
        }
        if (!BigRationalConstants.ZERO.equals(productivityBonus)) {
            fields.put("productivityBonus", String.valueOf(productivityBonus));
        }
        if (!BigRationalConstants.ZERO.equals(pollutionBonus)) {
            fields.put("pollutionBonus", String.valueOf(pollutionBonus));
        }
        if (!BigRationalConstants.ZERO.equals(speedBonus)) {
            fields.put("speedBonus", String.valueOf(speedBonus));
        }
    }

    @Override
    public String toString() {
        return Printable.toPrintableString(this);
    }

    public static class Builder {

        private BigRational consumptionBonus = BigRationalConstants.ZERO, productivityBonus =
                BigRationalConstants.ZERO, pollutionBonus = BigRationalConstants.ZERO, speedBonus =
                BigRationalConstants.ZERO;

        public Builder consumption(BigRational consumptionBonus) {
            this.consumptionBonus = consumptionBonus;
            return this;
        }

        public Builder productivity(BigRational productivityBonus) {
            this.productivityBonus = productivityBonus;
            return this;
        }

        public Builder pollution(BigRational pollutionBonus) {
            this.pollutionBonus = pollutionBonus;
            return this;
        }

        public Builder speed(BigRational speedBonus) {
            this.speedBonus = speedBonus;
            return this;
        }

        public ModuleEffect build() {
            return new ModuleEffect(consumptionBonus, productivityBonus, pollutionBonus, speedBonus);
        }
    }
}
