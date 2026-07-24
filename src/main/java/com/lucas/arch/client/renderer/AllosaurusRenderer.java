package com.lucas.arch.client.renderer;

import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.client.model.AllosaurusModel;

import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class AllosaurusRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AllosaurusEntity, R> {

    // Adicionamos a tipagem explícita <Boolean> para o compilador não se perder na inferência
    public static final RenderStateDataKey<Boolean> IS_MALE_KEY = 
        RenderStateDataKey.create(() -> "allosaurus_is_male");

    private static final Identifier TEXTURE_BABY = ArcheologyReimagined.id("textures/entity/allosaurus_baby.png");
    private static final Identifier TEXTURE_MALE = ArcheologyReimagined.id("textures/entity/allosaurus_male.png");
    private static final Identifier TEXTURE_FEMALE = ArcheologyReimagined.id("textures/entity/allosaurus_female.png");

    public AllosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AllosaurusModel());
        this.shadowRadius = 0.8f; 
    }

    @Override
    public void extractRenderState(AllosaurusEntity entity, R state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.isBaby = entity.getAgeTier() == AgeTier.BABY || entity.getAgeTier() == AgeTier.CHILD;

        state.setData(IS_MALE_KEY, entity.isMale());
    }

    @Override
    public Identifier getTextureLocation(R state) {
        if (state.isBaby) {
            return TEXTURE_BABY;
        }
        
        Boolean isMale = state.getData(IS_MALE_KEY);
        return (isMale != null && isMale) ? TEXTURE_MALE : TEXTURE_FEMALE;
    }
}