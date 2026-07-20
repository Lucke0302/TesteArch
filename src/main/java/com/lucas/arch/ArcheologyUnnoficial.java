package com.lucas.arch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lucas.arch.config.ModConfig;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModBlocks;
import com.lucas.arch.registry.ModDataComponentTypes;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.registry.ModMenuTypes;
import com.lucas.arch.registry.ModRecipeSerializers;
import com.lucas.arch.world.ModLootTableModifiers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.server.network.Filterable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import java.util.List;

public class ArcheologyUnnoficial implements ModInitializer {
    public static final String MOD_ID = "archeologyunnoficial";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ModDataComponentTypes.registerDataComponentTypes();
        
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModMenuTypes.registerMenuTypes();
        ModRecipeSerializers.registerRecipeSerializers();
        
        ModItems.registerModItems();
        ModLootTableModifiers.modifyLootTables();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            
            if (player.addTag("received_arch_guide")) {
                player.getInventory().add(createGuideBook());
            }
        });
    }

    /** Cria uma nova instancia do livro-guia com todas as paginas formatadas. */
    public static ItemStack createGuideBook() {
        ItemStack guideBook = new ItemStack(Items.WRITTEN_BOOK);

        // Página 1: Introdução
        MutableComponent page1 = Component.literal("Guia Arqueológico").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nBem-vindo ao ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Archeology Unofficial").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
            .append(Component.literal("!\n\nEste livro contém os manuais de operação para extração de DNA, síntese orgânica e fusão embrionária.").withStyle(ChatFormatting.DARK_GRAY));

        // Página 2: Mesa de Limpeza
        MutableComponent page2 = Component.literal("1. Mesa de Limpeza").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nRemove impurezas de fósseis (Planta, Réptil, Mamífero, Peixe).\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Requisitos:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("- Água (max 10 baldes)\n- Combustível de calor\n\nTempo: 10s (30% chance de DNA 40-85%).").withStyle(ChatFormatting.DARK_GRAY));

        // Página 3: Sintetizador
        MutableComponent page3 = Component.literal("2. O Sintetizador").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nSintetiza DNA puro e Matéria Orgânica em Embriões.\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Combustíveis Orgânicos:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("• Básico: -15% Qualidade\n• Médio: +0% Qualidade\n• Avançado: +15% Qualidade\n\nTempo: 60s. Falhas geram Aglomerados de Carne.").withStyle(ChatFormatting.DARK_GRAY));

        // Página 4: O Fusor
        MutableComponent page4 = Component.literal("3. O Fusor").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nFunde o Embrião em um Ovo biológico chocável.\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Bônus de Casca:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("• Galinha: +0% Estabilidade\n• Tartaruga: +15% Estabilidade\n• Sniffer: +30% Estabilidade\n\nTempo: 2 min. Falhas geram 3-6 carnes.").withStyle(ChatFormatting.DARK_GRAY));

        // Página 5: Qualidade e Genética
        MutableComponent page5 = Component.literal("Qualidade de DNA").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nA porcentagem de Qualidade do DNA define a taxa de sucesso nas máquinas e ditará os atributos dos dinossauros.\n\nUse combustíveis avançados e ovos raros para se aproximar de ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("100% de pureza").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .append(Component.literal("!").withStyle(ChatFormatting.DARK_GRAY));

        List<Filterable<Component>> pages = List.of(
            Filterable.passThrough(page1),
            Filterable.passThrough(page2),
            Filterable.passThrough(page3),
            Filterable.passThrough(page4),
            Filterable.passThrough(page5)
        );

        WrittenBookContent bookContent = new WrittenBookContent(
            Filterable.passThrough("Guia de Arqueologia"), 
            "Lucas.Moraes",
            0,
            pages,
            false
        );

        guideBook.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContent);
        return guideBook;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    
}