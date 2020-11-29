package io.github.ititus.factorio.recipes;

import io.github.ititus.data.CollectionUtil;
import io.github.ititus.factorio.recipes.data.FactorioData;
import io.github.ititus.factorio.recipes.data.item.module.ModuleEffect;
import io.github.ititus.factorio.recipes.data.prototype.PrototypeSet;
import io.github.ititus.factorio.recipes.data.prototype.entity.CraftingMachine;
import io.github.ititus.factorio.recipes.data.prototype.entity.Lab;
import io.github.ititus.factorio.recipes.data.prototype.item.Module;
import io.github.ititus.math.number.BigRational;
import io.github.ititus.math.permutation.Permutations;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.ititus.math.number.BigRationalConstants.ONE;
import static io.github.ititus.math.number.BigRationalConstants.ZERO;

public class ModuleTesting {

    private final PrototypeSet<Lab> labs;
    private final PrototypeSet<Module> modules;
    private final PrototypeSet<CraftingMachine> craftingMachines;

    public static void main(String[] args) {
        ModuleTesting mt = new ModuleTesting();
        mt.craftingMachine();
        mt.labSpeed();
    }

    private ModuleTesting() {
        Config.load(Path.of("config.json"));
        Config.offlineMode = false;
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

        private MachineStats(List<Module> machineModules, BigRational baseCraftingSpeed,
                             BigRational baseProductivityBonus,
                             BigRational speedBonusThroughTech, BigRational productivityBonusThroughTech,
                             ModuleEffect beaconEffect) {
            this.modules = machineModules;
            this.effect = ModuleEffect.of(machineModules)
                    .add(beaconEffect)
                    .add(ModuleEffect.builder().productivity(baseProductivityBonus).build())
                    .add(ModuleEffect.builder().productivity(productivityBonusThroughTech).build());
            this.speed = baseCraftingSpeed
                    .multiply(ONE.add(effect.getSpeedBonus()))
                    .multiply(ONE.add(speedBonusThroughTech));
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
        printMachineStats("assembling-machine-3", 8);
        printMachineStats("electric-furnace", 8);
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
        System.out.println(machine);

        Module speed3 = modules.get("speed-module-3");
        Module prod3 = modules.get("productivity-module-3");

        BigRational baseCraftingSpeed = machine.getCraftingSpeed();
        BigRational baseProductivityBonus = machine.getBaseProductivity();
        ModuleEffect beaconEffect = ModuleEffect.of(speed3, speed3).beaconized(beaconCount);

        BigRational speedBonusThroughTech = ZERO;
        BigRational productivityBonusThroughTech = ZERO;

        int slots = machine.getModuleSpecification().getModuleSlots();
        System.out.println("moduleSlots=" + slots);

        List<List<Module>> permutations = Permutations.permute(modules, slots);
        return permutations.stream()
                .map(p -> new MachineStats(p, baseCraftingSpeed, baseProductivityBonus, speedBonusThroughTech,
                        productivityBonusThroughTech, beaconEffect))
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .collect(Collectors.toUnmodifiableList());
    }

    private void labSpeed() {
        int beaconCount = 0;
        System.out.println("lab with " + beaconCount + " beacons");

        Lab lab = labs.get("lab");
        System.out.println(lab);

        Module speed3 = modules.get("speed-module-3");
        Module prod3 = modules.get("productivity-module-3");

        ModuleEffect beaconEffect = ModuleEffect.of(speed3, speed3).beaconized(beaconCount);

        BigRational baseResearchSpeed = lab.getResearchingSpeed();
        BigRational baseProductivityBonus = lab.getBaseProductivity();

        BigRational researchSpeedBonusThroughTech = BigRational.of(2.5);
        BigRational productivityBonusThroughTech = ZERO;

        int slots = lab.getModuleSpecification().getModuleSlots();
        System.out.println("moduleSlots=" + slots);

        List<List<Module>> permutations = Permutations.permute(modules, slots);
        permutations.stream()
                .map(p -> new MachineStats(p, baseResearchSpeed, baseProductivityBonus, researchSpeedBonusThroughTech,
                        productivityBonusThroughTech, beaconEffect))
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .forEachOrdered(System.out::println);
    }
}
