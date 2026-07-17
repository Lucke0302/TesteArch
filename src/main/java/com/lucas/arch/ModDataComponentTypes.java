package com.lucas.arch;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public class ModDataComponentTypes {

    public static final DataComponentType<Integer> DNA_QUALITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "dna_quality"),
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build()
    );

    public static void registerDataComponentTypes() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Data Components...");
    }
}