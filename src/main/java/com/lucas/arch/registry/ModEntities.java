package com.lucas.arch.registry;

import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;

import com.lucas.arch.ArcheologyUnnoficial;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<AllosaurusEntity> ALLOSAURUS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "allosaurus"),
        EntityType.Builder.of(AllosaurusEntity::new, MobCategory.CREATURE)
                .sized(1.5f, 2.0f)
                .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "allosaurus")))
    );

    public static void registerEntities() {
        ArcheologyUnnoficial.LOGGER.info("Registering entities for " + ArcheologyUnnoficial.MOD_ID);
    }

    public static void registerAttributes() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ALLOSAURUS, AllosaurusEntity.createAttributes());
    }
}