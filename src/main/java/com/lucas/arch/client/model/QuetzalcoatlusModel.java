package com.lucas.arch.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.QuetzalcoatlusEntity;

import net.minecraft.resources.Identifier;

public class QuetzalcoatlusModel extends GeoModel<QuetzalcoatlusEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "quetzalcoatlus");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return null;
    }

    @Override
    public Identifier getAnimationResource(QuetzalcoatlusEntity animatable) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "quetzalcoatlus");
    }
}