package com.lucas.arch.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.resources.Identifier;

public class AllosaurusModel extends GeoModel<AllosaurusEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "geckolib/models/allosaurus");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/entity/allosaurus_base.png");
    }

    @Override
    public Identifier getAnimationResource(AllosaurusEntity animatable) {
        return Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "geckolib/animations/allosaurus");
    }
}