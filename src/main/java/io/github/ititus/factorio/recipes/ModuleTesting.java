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
import java.util.stream.IntStream;

public class ModuleTesting {

    public static void main(String[] args) {
        craftingMachine();
        //labSpeed();
    }

    private static void craftingMachine() {
        Config.load(Path.of("config.json"));
        Config.offlineMode = true;
        Config.dumpLuaGlobals = false;
        FactorioData factorioData = FactorioData.loadFactorioData();

        PrototypeSet<CraftingMachine> craftingMachines = factorioData.getCraftingMachines();

        PrototypeSet<Module> allModules = factorioData.getModules();
        Module speed3 = allModules.get("speed-module-3");
        Module prod3 = allModules.get("productivity-module-3");
        PrototypeSet<Module> modules = new PrototypeSet<>(Module.class, List.of(speed3, prod3)).lock();
        modules.forEach(System.out::println);
        System.out.println();

        String machineName = "rocket-silo"; //"assembling-machine-3"; // "chemical-plant";
        CraftingMachine machine = craftingMachines.get(machineName);
        System.out.println(machine);
        System.out.println();

        int beaconCount = 20;//8;

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

        System.out.println("baseCraftingSpeed=" + baseCraftingSpeed);
        System.out.println("baseProductivityBonus=" + baseProductivityBonus);
        System.out.println("baseEffectiveCraftingSpeed=" + baseCraftingSpeed.multiply(BigRationalConstants.ONE.add(baseProductivityBonus)));
        System.out.println("beaconEffect=" + beaconEffect);
        System.out.println("outerEffect=" + outerEffect);
        System.out.println("baseSpeed=" + baseCraftingSpeed.multiply(BigRationalConstants.ONE.add(outerEffect.getSpeedBonus())));
        System.out.println("baseEffectiveSpeed=" + baseCraftingSpeed.multiply(BigRationalConstants.ONE.add(outerEffect.getSpeedBonus())).multiply(BigRationalConstants.ONE.add(outerEffect.getProductivityBonus())));
        System.out.println();

        int slots = machine.getModuleSpecification().getModuleSlots();
        System.out.println("moduleSlots=" + slots);

        List<List<Module>> permutations = Permutations.permute(modules, slots);
        // permutations.stream().map(l -> CollectionUtil.deepMap(l, Module::getName)).forEachOrdered(System
        // .out::println);

        permutations.stream()
                .map(p -> new Object() {
                    List<Module> modules = p;
                    ModuleEffect effect =
                            p.stream().map(Module::getEffect).reduce(ModuleEffect::add).orElse(ModuleEffect.NO_EFFECT).add(outerEffect);
                    BigRational speed =
                            baseCraftingSpeed.multiply(BigRationalConstants.ONE.add(effect.getSpeedBonus()));
                    BigRational effectiveSpeed =
                            speed.multiply(BigRationalConstants.ONE.add(effect.getProductivityBonus()));
                    BigRational demand =
                            BigRationalConstants.ONE.divide(BigRationalConstants.ONE.add(effect.getProductivityBonus()));
                })
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .map(o -> CollectionUtil.deepMap(o.modules, Module::getName) + ": speedBonus=" + o.effect.getSpeedBonus() + " | productivityBonus=" + o.effect.getProductivityBonus() + " | speed=" + o.speed + " | effSpeed=" + o.effectiveSpeed + " | consumption=" + o.demand)
                .forEachOrdered(System.out::println);
    }

    private static void labSpeed() {
        Config.load(Path.of("config.json"));
        Config.offlineMode = true;
        Config.dumpLuaGlobals = false;
        FactorioData factorioData = FactorioData.loadFactorioData();

        Lab lab = factorioData.getLabs().get("lab");

        PrototypeSet<Module> allModules = factorioData.getModules();
        Module speed3 = allModules.get("speed-module-3");
        Module prod3 = allModules.get("productivity-module-3");
        PrototypeSet<Module> modules = new PrototypeSet<>(Module.class, List.of(speed3, prod3)).lock();

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
                            baseResearchSpeed.multiply(BigRationalConstants.ONE.add(researchSpeedBonusThroughTech)).multiply(BigRationalConstants.ONE.add(effect.getSpeedBonus()));
                    BigRational effectiveSpeed =
                            speed.multiply(BigRationalConstants.ONE.add(effect.getProductivityBonus()));
                    BigRational demand =
                            BigRationalConstants.ONE.divide(BigRationalConstants.ONE.add(effect.getProductivityBonus()));
                })
                .sorted(Comparator.comparing(o -> o.effectiveSpeed))
                .map(o -> CollectionUtil.deepMap(o.modules, Module::getName) + ": speedBonus=" + o.effect.getSpeedBonus() + " | productivityBonus=" + o.effect.getProductivityBonus() + " | speed=" + o.speed + " | effSpeed=" + o.effectiveSpeed + " | consumption=" + o.demand)
                .forEachOrdered(System.out::println);

    }
}
