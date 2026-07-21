package com.lucas.arch.world;

import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.config.WorldGenMode;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Predicate;

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

        ResourceKey<PlacedFeature> bitterBerryCommonKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "bitter_berry_common"));
        ResourceKey<PlacedFeature> bitterBerryRareKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "bitter_berry_rare"));

        for (String biomeEntry : config.bitterBerryBiomes) {
            Predicate<BiomeSelectionContext> selector = resolveBiomeSelector(biomeEntry);
            if (selector == null) {
                ArcheologyUnnoficial.LOGGER.warn("[archeologyunnoficial] Entrada inválida em bitterBerryBiomes, ignorando: " + biomeEntry);
                continue;
            }
            BiomeModifications.addFeature(selector, GenerationStep.Decoration.VEGETAL_DECORATION, bitterBerryCommonKey);
            BiomeModifications.addFeature(selector, GenerationStep.Decoration.VEGETAL_DECORATION, bitterBerryRareKey);
        }
    }

    private static Predicate<BiomeSelectionContext> resolveBiomeSelector(String entry) {
        if (entry == null || entry.isBlank()) return null;

        try {
            if (entry.startsWith("#")) {
                Identifier tagId = Identifier.parse(entry.substring(1));
                TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagId);
                return BiomeSelectors.tag(tag);
            } else {
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, Identifier.parse(entry));
                return BiomeSelectors.includeByKey(biomeKey);
            }
        } catch (Exception e) {
            return null;
        }
    }
}