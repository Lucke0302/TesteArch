package com.lucas.arch.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.SpinosaurusEntity;

import net.minecraft.resources.Identifier;

public class SpinosaurusModel extends GeoModel<SpinosaurusEntity> {
    @Override public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "spinosaurus");
    }

    @Override public Identifier getTextureResource(GeoRenderState renderState) { return null; }

    @Override public Identifier getAnimationResource(SpinosaurusEntity animatable) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "spinosaurus");
    }
}