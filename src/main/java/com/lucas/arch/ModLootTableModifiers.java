package com.lucas.arch;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModLootTableModifiers {

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            
            ModConfig config = ModConfig.get();
            boolean isValidBlock = false;

            for (String blockLoot : config.fossilDropBlocks) {
                
                String[] parts = blockLoot.split(":");
                String namespace = parts.length == 2 ? parts[0] : "minecraft";
                String path = parts.length == 2 ? parts[1] : parts[0];
                
                ResourceKey<LootTable> configKey = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(namespace, path));
                
                if (key.equals(configKey)) {
                    isValidBlock = true;
                    break;
                }
            }

            if (isValidBlock) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemRandomChanceCondition.randomChance(config.fossilDropChance)) 

                        .add(LootItem.lootTableItem(ModItems.UNKNOWN_PLANT_FOSSIL).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.UNKNOWN_REPTILE_FOSSIL).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.UNKNOWN_FISH_FOSSIL).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.UNKNOWN_MAMMAL_FOSSIL).setWeight(1));

                tableBuilder.withPool(poolBuilder);
            }
        });
    }
}