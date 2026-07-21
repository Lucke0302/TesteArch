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
import com.lucas.arch.world.ModWorldGen;

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

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

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

        ModWorldGen.generateWorldGen();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            
            if (player.addTag("received_arch_guide")) {
                player.getInventory().add(createGuideBook());
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            
            if (stack.is(Items.BRUSH)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                BlockState aboveState = level.getBlockState(pos.above());

                if (aboveState.isAir()) {
                    Block targetBlock = null;

                    if (state.is(Blocks.SAND)) targetBlock = ModBlocks.BRUSHED_SAND;
                    else if (state.is(Blocks.GRAVEL)) targetBlock = ModBlocks.BRUSHED_GRAVEL;
                    else if (state.is(Blocks.TUFF)) targetBlock = ModBlocks.BRUSHED_TUFF;

                    if (targetBlock != null) {
                        if (!level.isClientSide()) {
                            level.setBlock(pos, targetBlock.defaultBlockState(), 3);
                        }
                        return InteractionResult.PASS;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static ItemStack createGuideBook() {
        ItemStack guideBook = new ItemStack(Items.WRITTEN_BOOK);

        MutableComponent page1 = Component.literal("Guia Arqueológico").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nBem-vindo ao ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Archeology Reimagined").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
            .append(Component.literal("!\n\nEste livro contém os manuais de operação para mineração, extração de DNA, síntese e botânica.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page2 = Component.literal("1. Mesa de Limpeza").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nRemove impurezas de fósseis.\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Requisitos:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("- Água (max 10 baldes)\n- Combustível de calor\n\nDropa DNA puro ou DNA Fragmentado (20% chance). Aceita Mosquitos no Âmbar.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page3 = Component.literal("2. O Sintetizador").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nSintetiza DNA puro e Matéria Orgânica em Embriões.\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Combustíveis Orgânicos:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("Básico: -15% Qualidade\nMédio: +0% Qualidade\nAvançado: +15% Qualidade").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page4 = Component.literal("3. O Fusor").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nFunde o Embrião em um Ovo biológico chocável.\n\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Bônus de Casca:\n").withStyle(ChatFormatting.BLACK, ChatFormatting.UNDERLINE))
            .append(Component.literal("Galinha: +0%\nTartaruga: +15%\nSniffer: +30%\n\nTempo: 2 min.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page5 = Component.literal("4. Escavação").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nFósseis e minérios de ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Âmbar").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" geram no subsolo.\n\nNa superfície, use um ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Pincel").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
            .append(Component.literal(" em Areia, Cascalho ou Tufo. Escovar gera pós e tem 2% de chance de revelar Fósseis ou Ovos de Sniffer!").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page6 = Component.literal("5. Botânica Exótica").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nAs ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Bagas Amargas").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
            .append(Component.literal(" são perigosas. Seu arbusto espinhoso prende e envenena intrusos. \n\nEmbora nutritivas, ingerir as frutas causa Lentidão severa.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page7 = Component.literal("6. Química Avançada").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nMisture Pólvora, Açúcar e Sementes para criar o ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Biopropelente").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
            .append(Component.literal(".\n\nUse Seringas e Dardos no Biocatalisador com DNA Fragmentado para criar aditivos genéticos e armas tranquilizantes.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page8 = Component.literal("7. Compactação").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nPara não lotar o inventário com a poeira da escavação, preencha uma Bancada de Trabalho (3x3) com ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("9 Pós").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .append(Component.literal(" de Areia, Cascalho ou Tufo para recriar o respectivo bloco maciço original.").withStyle(ChatFormatting.DARK_GRAY));

        List<Filterable<Component>> pages = List.of(
            Filterable.passThrough(page1), Filterable.passThrough(page2),
            Filterable.passThrough(page3), Filterable.passThrough(page4),
            Filterable.passThrough(page5), Filterable.passThrough(page6),
            Filterable.passThrough(page7), Filterable.passThrough(page8)
        );

        WrittenBookContent bookContent = new WrittenBookContent(Filterable.passThrough("Guia de Arqueologia"), "Lucas.Moraes", 0, pages, false);
        guideBook.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContent);
        return guideBook;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    
}