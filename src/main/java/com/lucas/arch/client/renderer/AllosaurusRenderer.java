package com.lucas.arch.client.renderer;

import org.jspecify.annotations.Nullable;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.lucas.arch.client.model.AllosaurusModel;
import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

import com.lucas.arch.ArcheologyUnnoficial;

public class AllosaurusRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AllosaurusEntity, R> {

    public static final com.geckolib.constant.dataticket.DataTicket<Float> VISUAL_SCALE =
        com.geckolib.constant.dataticket.DataTicket.create(
            ArcheologyUnnoficial.MOD_ID + ":visual_scale", Float.class);

    public AllosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new AllosaurusModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void addRenderData(AllosaurusEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(VISUAL_SCALE, animatable.getVisualScale());
    }

    @Override
    public void scaleModelForRender(com.geckolib.renderer.base.RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(VISUAL_SCALE, 1.0f);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}