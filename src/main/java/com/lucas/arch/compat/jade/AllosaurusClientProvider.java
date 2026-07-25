package com.lucas.arch.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import com.lucas.arch.entity.Feeling;

public enum AllosaurusClientProvider implements IEntityComponentProvider {
    INSTANCE;

    private static final Identifier ID =
        Identifier.fromNamespaceAndPath("archeology_reimagined", "allosaurus_data");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        data.getString("PrimaryTrait").ifPresent(primaryRaw -> {
            String secondaryRaw = data.getString("SecondaryTrait").orElse("");

            MutableComponent primary = Component.translatable("trait.archeology_reimagined." + primaryRaw.toLowerCase());
            MutableComponent traitsComp = Component.literal("  ").append(primary);

            if (!secondaryRaw.isEmpty()) {
                traitsComp.append(" / ").append(Component.translatable("trait.archeology_reimagined." + secondaryRaw.toLowerCase()));
            }

            tooltip.add(Component.translatable("tooltip.archeology_reimagined.personality").withStyle(ChatFormatting.GRAY));
            tooltip.add(traitsComp.withStyle(ChatFormatting.GOLD));
        });

        data.getByte("DominantState").ifPresent(stateId -> {
            MutableComponent stateComponent;
            
            if (stateId > 0 && stateId <= Feeling.values().length) {
                Feeling dominantFeeling = Feeling.values()[stateId - 1];
                stateComponent = Component.translatable("feeling.archeology_reimagined." + dominantFeeling.name().toLowerCase());
                
                switch (dominantFeeling) {
                    case ANGER -> stateComponent.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                    case FEAR -> stateComponent.withStyle(ChatFormatting.YELLOW);
                    case CURIOSITY -> stateComponent.withStyle(ChatFormatting.AQUA);
                    case HUNGER -> stateComponent.withStyle(ChatFormatting.GREEN);
                }
            } else {
                stateComponent = Component.translatable("feeling.archeology_reimagined.neutral").withStyle(ChatFormatting.WHITE);
            }

            tooltip.add(Component.translatable("tooltip.archeology_reimagined.state").withStyle(ChatFormatting.GRAY).append(stateComponent));
        });
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}