package com.lucas.arch.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    
    public static final SoundEvent ALLO_AMBIENT = registerSoundEvent("entity.allosaurus.ambient");
    public static final SoundEvent ALLO_HURT = registerSoundEvent("entity.allosaurus.hurt");
    public static final SoundEvent ALLO_DEATH = registerSoundEvent("entity.allosaurus.death");

    public static final SoundEvent SPINO_AMBIENT = registerSoundEvent("entity.spinosaurus.ambient");
    public static final SoundEvent SPINO_HURT = registerSoundEvent("entity.spinosaurus.hurt");
    public static final SoundEvent SPINO_DEATH = registerSoundEvent("entity.spinosaurus.death");

    public static final SoundEvent QUETZAL_AMBIENT = registerSoundEvent("entity.quetzalcoatlus.ambient");
    public static final SoundEvent QUETZAL_HURT = registerSoundEvent("entity.quetzalcoatlus.hurt");
    public static final SoundEvent QUETZAL_DEATH = registerSoundEvent("entity.quetzalcoatlus.death");

    public static final SoundEvent PARASAUR_AMBIENT = registerSoundEvent("entity.parasaurolophus.ambient");
    public static final SoundEvent PARASAUR_HURT = registerSoundEvent("entity.parasaurolophus.hurt");
    public static final SoundEvent PARASAUR_DEATH = registerSoundEvent("entity.parasaurolophus.death");

    public static final SoundEvent PACHY_AMBIENT = registerSoundEvent("entity.pachycephalosaurus.ambient");
    public static final SoundEvent PACHY_HURT = registerSoundEvent("entity.pachycephalosaurus.hurt");
    public static final SoundEvent PACHY_DEATH = registerSoundEvent("entity.pachycephalosaurus.death");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("archeology_reimagined", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
    }
}