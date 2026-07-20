package com.lucas.arch.world;

import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.config.WorldGenMode;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModWorldGen {
    public static void generateWorldGen() {
        ModConfig config = ModConfig.get();

        if (config.worldGenMode == WorldGenMode.CLASSIC) {
            return;
        }

        String density = config.fossilDensity.name().toLowerCase();

        ResourceKey<PlacedFeature> fossilPlacedKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "fossil_ore_" + density));
        ResourceKey<PlacedFeature> amberPlacedKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "amber_ore_" + density));

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, fossilPlacedKey);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, amberPlacedKey);
    }
}