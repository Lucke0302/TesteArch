package com.lucas.arch.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.lucas.arch.client.model.AllosaurusModel;
import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AllosaurusRenderer extends GeoEntityRenderer<AllosaurusEntity, AllosaurusRenderState> {
    public AllosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new AllosaurusModel());
        this.shadowRadius = 0.5f;
    }
}