package com.lucas.arch.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.Map;

public class AllosaurusRenderState extends LivingEntityRenderState implements GeoRenderState {
    public int color;
    public float scale;

    private final Map<DataTicket<?>, Object> dataMap = new Reference2ObjectOpenHashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.dataMap;
    }
}