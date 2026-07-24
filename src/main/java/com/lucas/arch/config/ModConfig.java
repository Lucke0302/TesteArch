package com.lucas.arch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lucas.arch.ArcheologyReimagined;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static ModConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final Map<String, String> _comment = buildComments();

    public WorldGenMode worldGenMode = WorldGenMode.REIMAGINED;
    public FossilDensity fossilDensity = FossilDensity.MEDIUM;

    public int cleansingTableTicks = 200;
    public int synthesizerTicks = 1200;
    public int fuserTicks = 2400;
    public int cleansingTableWaterCost = 1;
    public int biocatalyzerTicks = 400;

    public float fossilDropChance = 0.01f;
    public List<String> fossilDropBlocks = Arrays.asList(
            "minecraft:blocks/sand",
            "minecraft:blocks/gravel",
            "minecraft:blocks/tuff"
    );

    public List<String> bitterBerryBiomes = Arrays.asList(
            "#minecraft:is_forest"
    );

    private static Map<String, String> buildComments() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("worldGenMode", "CLASSIC = nada de custom worldgen | ORIGINAL = mantém loot original vanilla | REIMAGINED = usa todo o worldgen custom do mod");
        m.put("fossilDensity", "LOW, MEDIUM ou HIGH — controla quantos fossil_ore/amber_ore geram por chunk");
        m.put("cleansingTableTicks", "Tempo (em ticks, 20 = 1s) pra Mesa de Limpeza processar 1 fóssil");
        m.put("synthesizerTicks", "Tempo (em ticks) pro Sintetizador gerar 1 embrião");
        m.put("fuserTicks", "Tempo (em ticks) pro Fusor gerar 1 ovo");
        m.put("cleansingTableWaterCost", "Quantos 'baldes' de água (de 0 a 10) a Mesa de Limpeza consome por processo");
        m.put("fossilDropChance", "Chance (0.0 a 1.0) de um bloco em fossilDropBlocks dropar fóssil ao cair/quebrar");
        m.put("fossilDropBlocks", "Lista de blocos (formato namespace:blocks/path) elegíveis a dropar fóssil");
        m.put("bitterBerryBiomes", "Biomas onde Bitter Berry nasce. Use '#namespace:tag' para tags de bioma (ex: '#minecraft:is_forest', '#minecraft:is_taiga') ou 'namespace:biome_id' para um bioma específico (ex: 'minecraft:taiga'). REQUER MUNDO NOVO OU /reload PRA APLICAR");
        m.put("_nota_raridade", "A raridade de Bitter/Sweet Berry (chance de spawn) NÃO é configurável neste arquivo — está fixa nos arquivos de worldgen do mod. Alterar isso requer editar o código-fonte e recompilar.");
        return m;
    }

    public static ModConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ArcheologyReimagined.MOD_ID + ".json");
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
                if (instance.bitterBerryBiomes == null || instance.bitterBerryBiomes.isEmpty()) {
                    instance.bitterBerryBiomes = Arrays.asList("#minecraft:is_forest");
                }

                instance._comment.clear();
                instance._comment.putAll(buildComments());

                save(configFile);

            } catch (Exception e) {
                ArcheologyReimagined.LOGGER.error("Erro ao ler a config ou arquivo corrompido! Recriando padrao.", e);
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
            ArcheologyReimagined.LOGGER.error("Erro ao salvar a config!", e);
        }
    }
}