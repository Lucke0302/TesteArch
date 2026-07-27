package com.lucas.arch.registry;

import com.lucas.arch.ArcheologyReimagined;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public class ModDataComponentTypes {

    public static final DataComponentType<Integer> DNA_QUALITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "dna_quality"),
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build()
    );

    public static final DataComponentType<Identifier> SYRINGE_SPECIES = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "syringe_species"),
        DataComponentType.<Identifier>builder()
            .persistent(Identifier.CODEC)
            .networkSynchronized(Identifier.STREAM_CODEC)
            .build()
    );

    public static void registerDataComponentTypes() {
        System.out.println("[" + ArcheologyReimagined.MOD_ID + "] Registrando Data Components...");
    }
}