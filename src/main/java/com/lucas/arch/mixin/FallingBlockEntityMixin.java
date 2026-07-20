package com.lucas.arch.mixin;

import com.lucas.arch.config.ModConfig;
import com.lucas.arch.config.WorldGenMode;
import com.lucas.arch.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {

    @Inject(method = "onDestroyedOnLanding", at = @At("HEAD"))
    private void onFallingBlockDestroyed(CallbackInfo ci) {
        FallingBlockEntity entity = (FallingBlockEntity) (Object) this;
        ModConfig config = ModConfig.get();

        if (config.worldGenMode == WorldGenMode.ORIGINAL || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock());

        boolean isValidBlock = config.fossilDropBlocks.stream().anyMatch(entry -> {
            String cleanEntry = entry.replace("blocks/", "");
            return cleanEntry.equals(blockId.toString());
        });

        if (isValidBlock && serverLevel.getRandom().nextFloat() < config.fossilDropChance) {
            List<Item> fossils = List.of(
                ModItems.UNKNOWN_PLANT_FOSSIL,
                ModItems.UNKNOWN_REPTILE_FOSSIL,
                ModItems.UNKNOWN_FISH_FOSSIL,
                ModItems.UNKNOWN_MAMMAL_FOSSIL
            );
            Item chosenFossil = fossils.get(serverLevel.getRandom().nextInt(fossils.size()));

            entity.spawnAtLocation(serverLevel, chosenFossil);
        }
    }
}