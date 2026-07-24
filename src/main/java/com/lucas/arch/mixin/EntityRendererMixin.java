/*package com.lucas.arch.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.client.renderer.AllosaurusRenderer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void arch$extractCustomState(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        
        if (entity instanceof AllosaurusEntity allo && state instanceof LivingEntityRenderState livingState) {
            
            livingState.isBaby = allo.getAgeTier() == AgeTier.BABY || allo.getAgeTier() == AgeTier.CHILD;
            
            livingState.addGeckolibData(AllosaurusRenderer.IS_MALE_TICKET, allo.isMale());
        }
    }
}*/