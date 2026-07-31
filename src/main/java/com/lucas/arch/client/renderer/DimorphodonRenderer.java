package com.lucas.arch.client.renderer;

import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.DimorphodonEntity;
import com.lucas.arch.client.model.DimorphodonModel;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class DimorphodonRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<DimorphodonEntity, R> {
    
    public static final RenderStateDataKey<Boolean> IS_MALE_KEY = RenderStateDataKey.create(() -> "dimorphodon_is_male");
    public static final RenderStateDataKey<Boolean> IS_BABY_KEY = RenderStateDataKey.create(() -> "dimorphodon_is_baby");

    private static final Identifier TEXTURE_BABY = ArcheologyReimagined.id("textures/entity/dimorphodon_baby.png");
    private static final Identifier TEXTURE_MALE = ArcheologyReimagined.id("textures/entity/dimorphodon_male.png");
    private static final Identifier TEXTURE_FEMALE = ArcheologyReimagined.id("textures/entity/dimorphodon_female.png");

    public DimorphodonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DimorphodonModel());
        this.shadowRadius = 0.3f; 
    }

    @Override
    public void extractRenderState(DimorphodonEntity entity, R state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        AgeTier age = entity.getAgeTier();
        state.setData(IS_BABY_KEY, age == AgeTier.BABY || age == AgeTier.CHILD);
        state.setData(IS_MALE_KEY, entity.isMale());
    }

    @Override
    public Identifier getTextureLocation(R state) {
        Boolean isBaby = state.getData(IS_BABY_KEY);
        if (isBaby != null && isBaby) return TEXTURE_BABY;
        
        Boolean isMale = state.getData(IS_MALE_KEY);
        return (isMale != null && isMale) ? TEXTURE_MALE : TEXTURE_FEMALE;
    }
}