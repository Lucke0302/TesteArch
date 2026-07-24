package com.lucas.arch.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.renderer.AllosaurusRenderer;
import net.minecraft.resources.Identifier;

public class AllosaurusModel extends GeoModel<AllosaurusEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "allosaurus");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.isBaby) {
            return ArcheologyReimagined.id("textures/entity/allosaurus_baby.png");
        }
        Boolean isMale = renderState.getData(AllosaurusRenderer.IS_MALE_KEY);
        return (isMale != null && isMale)
                ? ArcheologyReimagined.id("textures/entity/allosaurus_male.png")
                : ArcheologyReimagined.id("textures/entity/allosaurus_female.png");
    }

    @Override
    public Identifier getAnimationResource(AllosaurusEntity animatable) {
        return Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "allosaurus");
    }
}