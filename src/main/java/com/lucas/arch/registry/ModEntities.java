package com.lucas.arch.registry;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.PachycephalosaurusEntity;
import com.lucas.arch.entity.ParasaurolophusEntity;
import com.lucas.arch.entity.SpinosaurusEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;

import com.lucas.arch.ArcheologyReimagined;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModEntities {

    public static final EntityType<AllosaurusEntity> ALLOSAURUS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "allosaurus"),
        EntityType.Builder.of(AllosaurusEntity::new, MobCategory.CREATURE)
                .sized(1.2f, 1.1f)
                .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "allosaurus")))
    );

    public static final EntityType<PachycephalosaurusEntity> PACHYCEPHALOSAURUS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "pachycephalosaurus"),
        EntityType.Builder.of(PachycephalosaurusEntity::new, MobCategory.CREATURE)
                .sized(0.9f, 0.8f) 
                .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "pachycephalosaurus")))
    );

    public static final EntityType<SpinosaurusEntity> SPINOSAURUS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "spinosaurus"),
        EntityType.Builder.of(SpinosaurusEntity::new, MobCategory.CREATURE)
                .sized(1.4f, 1.3f)
                .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "spinosaurus")))
    );

    public static final EntityType<ParasaurolophusEntity> PARASAUROLOPHUS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "parasaurolophus"),
        EntityType.Builder.of(ParasaurolophusEntity::new, MobCategory.CREATURE)
                .sized(1.4f, 1.3f)
                .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "parasaurolophus")))
    );

    public static void registerEntities() {
        ArcheologyReimagined.LOGGER.info("Registering entities for " + ArcheologyReimagined.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(ALLOSAURUS, AllosaurusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PACHYCEPHALOSAURUS, PachycephalosaurusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SPINOSAURUS, SpinosaurusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PARASAUROLOPHUS, ParasaurolophusEntity.createAttributes());
    }
}