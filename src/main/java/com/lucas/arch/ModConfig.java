package com.lucas.arch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public float fossilDropChance = .01f;
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
            } catch (IOException e) {
                ArcheologyUnnoficial.LOGGER.error("Erro ao ler o arquivo de configuração! Usando padrão da memória.", e);
                instance = new ModConfig();
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
            ArcheologyUnnoficial.LOGGER.error("Erro ao gerar o arquivo de configuração!", e);
        }
    }
}