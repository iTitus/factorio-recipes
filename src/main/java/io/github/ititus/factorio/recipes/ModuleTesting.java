package io.github.ititus.factorio.recipes;

import io.github.ititus.data.CollectionUtil;
import io.github.ititus.factorio.recipes.data.FactorioData;
import io.github.ititus.factorio.recipes.data.item.module.ModuleEffect;
import io.github.ititus.factorio.recipes.data.prototype.PrototypeSet;
import io.github.ititus.factorio.recipes.data.prototype.entity.CraftingMachine;
import io.github.ititus.factorio.recipes.data.prototype.entity.Lab;
import io.github.ititus.factorio.recipes.data.prototype.item.Module;
import io.github.ititus.math.number.BigRational;
import io.github.ititus.math.number.BigRationalConstants;
import io.github.ititus.math.permutation.Permutations;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.ititus.math.number.BigRationalConstants.ONE;

public class ModuleTesting {

    private final PrototypeSet<Lab> labs;
    private final PrototypeSet<Module> modules;
    private final PrototypeSet<CraftingMachine> craftingMachines;

    public static void main(String[] args) {
        new ModuleTesting().craftingMachine();
        //new ModuleTesting().labSpeed();
    }

    private ModuleTesting() {
        Config.load(Path.of("config.json"));
        Config.offlineMode = true;
        Config.dumpLuaGlobals = false;
        FactorioData factorioData = FactorioData.loadFactorioData();

        this.craftingMachines = factorioData.getCraftingMachines();

        PrototypeSet<Module> allModules = factorioData.getModules();
        Module speed3 = allModules.get("speed-module-3");
        Module prod3 = allModules.get("productivity-module-3");
        this.modules = new PrototypeSet<>(Module.class, List.of(speed3, prod3)).lock();
        //modules.forEach(System.out::println);
        //System.out.println();

        this.labs = factorioData.getLabs();
    }

    private static class MachineStats {

        private final List<Module> modules;
        private final ModuleEffect effect;
        private final BigRational speed;
        private final BigRational effectiveSpeed;
        private final BigRational demand;

        private MachineStats(List<Module> machineModules, BigRational baseCraftingSpeed, ModuleEffect outerEffect) {
            this.modules = machineModules;
            this.effect =
                    machineModules.stream().map(Module::getEffect).reduce(ModuleEffect::add).orElse(ModuleEffect.NO_EFFECT).add(outerEffect);
            this.speed = baseCraftingSpeed.multiply(ONE.add(effect.getSpeedBonus()));
            this.effectiveSpeed = speed.multiply(ONE.add(effect.getProductivityBonus()));
            this.demand = ONE.divide(ONE.add(effect.getProductivityBonus()));
        }

        @Override
        public String toString() {
            return CollectionUtil.deepMap(modules, Module::getName) + ": speedBonus=" + effect.getSpeedBonus() + " | " +
                    "productivityBonus=" + effect.getProductivityBonus() + " | speed=" + speed + " | effSpeed=" + effectiveSpeed + " | consumption=" + demand;
        }
    }

    private void craftingMachine() {
        // "electric-furnace"
        printMachineStats("assembling-machine-3", 8);
        printMachineStats("chemical-plant", 8);
        printMachineStats("rocket-silo", 20);
    }

    private void printMachineStats(String machineName, int beaconCount) {
        System.out.println(machineName + " with " + beaconCount + " beacons");
        getMachineStats(machineName, beaconCount).forEach(System.out::println);
        System.out.println();
    }

    private List<MachineStats> getMachineStats(String machineName, int beaconCount) {
        CraftingMachine machine = craftingMachines.get(machineName);
        //System.out.println(machine);
        //System.out.println();

        Module speed3 = modules.get("speed-module-3");
        Module prod3 = modules.get("productivity-module-3");

        BigRational baseCraftingSpeed = machine.getCraftingSpeed();
        BigRational baseProductivityBonus = machine.getBaseProductivity();
        ModuleEffect beaconEffect = IntStream.range(0, beaconCount)
                .mapToObj(i -> List.of(speed3, speed3))
                .flatMap(List::stream)
                .map(Module::getEffect)
                .reduce(ModuleEffect::add)
                .orElse(ModuleEffect.NO_EFFECT)
                .multiply(BigRationalConstants.ONE_OVER_TWO);

        ModuleEffect outerEffect =
                machine.filter(beaconEffect.add(ModuleEffect.builder().productivity(baseProductivityBonus).build()));

        //System.out.println("baseCraftingSpeed=" + baseCraftingSpeed);
        //System.out.println("baseProductivityBonus=" + baseProductivityBonus);
        //System.out.println("baseEffectiveCraftingSpeed=" + baseCraftingSpeed.multiply(ONE.add
        // (baseProductivityBonus)));
        //System.out.println("beaconEffect=" + beaconEffect);
        //System.out.println("outerEffect=" + outerEffect);
        //System.out.println("baseSpeed=" + baseCraftingSpeed.multiply(ONE.add(outerEffect.getSpeedBonus())));
        //System.out.println("baseEffectiveSpeed=" + baseCraftingSpeed.multiply(ONE.add(outerEffect.getSpeedBonus()))
        // .multiply(ONE.add(outerEffect.getProductivityBonus())));
        //System.out.println();

        int slots = machine.getModuleSpecification().getModuleSlots();
        System.out.println("moduleSlots=" + slots);

        List<List<Module>> permutations = Permutations.permute(modules, slots);
        // permutations.stream().map(l -> CollectionUtil.deepMap(l, Module::getName)).forEachOrdered(System
        // .out::println);

        return permutations.stream()
                .map(p -> new MachineStats(p, baseCraftingSpeed, outerEffect))
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .collect(Collectors.toUnmodifiableList());
    }

    private void labSpeed() {
        Lab lab = labs.get("lab");

        Module speed3 = modules.get("speed-module-3");
        Module prod3 = modules.get("productivity-module-3");

        int beaconCount = 12;
        ModuleEffect beaconEffect = IntStream.range(0, beaconCount)
                .mapToObj(i -> List.of(speed3, speed3))
                .flatMap(List::stream)
                .map(Module::getEffect)
                .reduce(ModuleEffect::add)
                .orElse(ModuleEffect.NO_EFFECT)
                .multiply(BigRationalConstants.ONE_OVER_TWO);

        // beaconEffect = ModuleEffect.NO_EFFECT;

        BigRational baseResearchSpeed = lab.getResearchingSpeed();
        BigRational researchSpeedBonusThroughTech = BigRational.of(2.5);
        BigRational baseProductivityBonus = lab.getBaseProductivity();

        ModuleEffect outerEffect = beaconEffect.add(ModuleEffect.builder().productivity(baseProductivityBonus).build());

        int slots = lab.getModuleSpecification().getModuleSlots();

        Permutations.permute(modules, slots).stream()
                .map(p -> new Object() {
                    List<Module> modules = p;
                    ModuleEffect effect =
                            p.stream().map(Module::getEffect).reduce(ModuleEffect::add).orElse(ModuleEffect.NO_EFFECT).add(outerEffect);
                    BigRational speed =
                            baseResearchSpeed.multiply(ONE.add(researchSpeedBonusThroughTech)).multiply(ONE.add(effect.getSpeedBonus()));
                    BigRational effectiveSpeed =
                            speed.multiply(ONE.add(effect.getProductivityBonus()));
                    BigRational demand =
                            ONE.divide(ONE.add(effect.getProductivityBonus()));
                })
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .map(o -> CollectionUtil.deepMap(o.modules, Module::getName) + ": speedBonus=" + o.effect.getSpeedBonus() + " | productivityBonus=" + o.effect.getProductivityBonus() + " | speed=" + o.speed + " | effSpeed=" + o.effectiveSpeed + " | consumption=" + o.demand)
                .forEachOrdered(System.out::println);
    }
}
