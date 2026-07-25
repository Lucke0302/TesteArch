// compat/jade/AllosaurusEggClientProvider.java
package com.lucas.arch.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AllosaurusEggClientProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier ID =
        Identifier.fromNamespaceAndPath("archeology_reimagined", "allosaurus_egg");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        data.getInt("HatchProgress").ifPresent(progress ->
            tooltip.add(Component.literal("Eclosão: " + progress + "%")
                .withStyle(ChatFormatting.GOLD))
        );
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}