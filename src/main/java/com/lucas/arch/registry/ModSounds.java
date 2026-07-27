package com.lucas.arch.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    
    public static final SoundEvent ALLO_AMBIENT = registerSoundEvent("entity.allosaurus.ambient");
    public static final SoundEvent ALLO_HURT = registerSoundEvent("entity.allosaurus.hurt");
    public static final SoundEvent ALLO_DEATH = registerSoundEvent("entity.allosaurus.death");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("archeology_reimagined", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
    }
}