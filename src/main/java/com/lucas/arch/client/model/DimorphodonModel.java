package com.lucas.arch.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.DimorphodonEntity;
import net.minecraft.resources.Identifier;

public class DimorphodonModel extends GeoModel<DimorphodonEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "dimorphodon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return null;
    }

    @Override
    public Identifier getAnimationResource(DimorphodonEntity animatable) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "dimorphodon");
    }
}