package com.lucas.arch.client.renderer;

import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.client.model.AllosaurusModel;
import com.lucas.arch.entity.AllosaurusEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AllosaurusRenderer extends GeoEntityRenderer<AllosaurusEntity, LivingEntityRenderState> {


    public static final DataTicket<Boolean> IS_MALE_TICKET = DataTicket.create("is_male", Boolean.class);

    private static final Identifier TEXTURE_BABY = ArcheologyReimagined.id("textures/entity/allosaurus_baby.png");
    private static final Identifier TEXTURE_MALE = ArcheologyReimagined.id("textures/entity/allosaurus_male.png");
    private static final Identifier TEXTURE_FEMALE = ArcheologyReimagined.id("textures/entity/allosaurus_female.png");

    public AllosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AllosaurusModel());
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState renderState) {
        // A flag isBaby já existe e será preenchida nativamente pelo nosso Mixin
        if (renderState.isBaby) {
            return TEXTURE_BABY;
        }
        
        // Resgatamos o dado usando a Duck Interface do GeckoLib acoplada no estado
        Boolean isMale = renderState.getOrDefaultGeckolibData(IS_MALE_TICKET, true);
        return Boolean.TRUE.equals(isMale) ? TEXTURE_MALE : TEXTURE_FEMALE;
    }
}