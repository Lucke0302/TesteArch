package com.lucas.arch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lucas.arch.ArcheologyUnnoficial;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    private static ModConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public WorldGenMode worldGenMode = WorldGenMode.REIMAGINED;
    public FossilDensity fossilDensity = FossilDensity.MEDIUM;

    public int cleansingTableTicks = 200;
    public int synthesizerTicks = 1200;
    public int fuserTicks = 2400;
    public int cleansingTableWaterCost = 1;
    
    public float fossilDropChance = 0.01f;
    public List<String> fossilDropBlocks = Arrays.asList(
            "minecraft:blocks/sand",
            "minecraft:blocks/gravel",
            "minecraft:blocks/tuff"
    );

    public static ModConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ArcheologyUnnoficial.MOD_ID + ".json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, ModConfig.class);
                
                if (instance.worldGenMode == null) {
                    instance.worldGenMode = WorldGenMode.REIMAGINED;
                }
                if (instance.fossilDensity == null) {
                    instance.fossilDensity = FossilDensity.MEDIUM;
                }
                if (instance.fossilDropBlocks == null || instance.fossilDropBlocks.isEmpty()) {
                    instance.fossilDropBlocks = Arrays.asList("minecraft:blocks/sand", "minecraft:blocks/gravel", "minecraft:blocks/tuff");
                }
                
                save(configFile);
                
            } catch (Exception e) {
                ArcheologyUnnoficial.LOGGER.error("Erro ao ler a config ou arquivo corrompido! Recriando padrao.", e);
                instance = new ModConfig();
                save(configFile);
            }
        } else {
            instance = new ModConfig();
            save(configFile);
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            ArcheologyUnnoficial.LOGGER.error("Erro ao salvar a config!", e);
        }
    }
}