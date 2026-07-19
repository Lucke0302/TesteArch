package com.lucas.arch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        MutableComponent page1 = Component.literal("Guia Arqueologico").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nBem-vindo ao ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Archeology Unofficial").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("!\n\nEste livro contem nossos registros iniciais sobre a extracao de DNA antigo e restauracao fossil.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page2 = Component.literal("Mesa de Limpeza").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nA maquina principal. Ela requer:\n- ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Agua").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" (max 10 baldes)\n- ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Combustivel").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("\n\nCada processo leva 10s (200 ticks) e consome agua.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page3 = Component.literal("Os Fosseis").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nAte o momento, catalogamos 4 matrizes base:\n\n- Planta\n- Reptil\n- Mamifero\n- Peixe\n\nPurifique-os na Mesa.").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent page4 = Component.literal("Extracao de DNA").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD)
            .append(Component.literal("\n\nHa apenas 30% de chance de sucesso. O resultado e um DNA puro (40% a 85%).\n\nFalhas destroem o material (areia, cascalho, osso ou carvao).").withStyle(ChatFormatting.DARK_GRAY));

        List<Filterable<Component>> pages = List.of(
            Filterable.passThrough(page1),
            Filterable.passThrough(page2),
            Filterable.passThrough(page3),
            Filterable.passThrough(page4)
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