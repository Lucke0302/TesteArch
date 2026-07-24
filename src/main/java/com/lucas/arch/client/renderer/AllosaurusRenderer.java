package com.lucas.arch.client.renderer;

import org.jspecify.annotations.Nullable;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.client.model.AllosaurusModel;
import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class AllosaurusRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AllosaurusEntity, R> {
    
    public AllosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new AllosaurusModel());
        this.shadowRadius = 0.5f;
    }

    // Métodos addRenderData e scaleModelForRender foram removidos.
    // O Minecraft 1.21.2+ irá escalar a hitbox e o modelo visual automaticamente
    // lendo o Attributes.SCALE que você implementou na entidade.
}