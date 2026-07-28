# 📦 Mapeamento de Imports: Archeology Reimagined (Minecraft 1.21+)

Este documento cataloga os **imports essenciais e atualizados** do projeto, organizados por **função e módulo de desenvolvimento**. 

Como as atualizações recentes da API do Minecraft e do ecossistema Fabric alteraram drasticamente diversas estruturas base (como NBT sendo substituído por Componentes, renderização desacoplada e o novo fluxo de consumíveis), este mapeamento serve como uma referência rápida e ancoragem. Ao fornecer este documento como contexto, evita-se que assistentes de IA se confundam e alucinem chamadas a classes obsoletas durante a refatoração ou criação de novas features.

---

## 1. UI e Renderização de Telas (Screens & Menus)
O pipeline de renderização de interface foi profundamente reestruturado, substituindo as chamadas antigas diretas pela extração de gráficos via `RenderPipelines`.

// Renderização moderna de GUI
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

// Classes base de UI
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;

*Onde encontrar:* `BiocatalyzerScreen`, `SynthesizerScreen`, `FuserMenu`, `CleansingTableMenu`.

## 2. Sistema de Componentes (Data Components)
Substituto absoluto do antigo sistema NBT para instâncias de itens, garantindo manipulação tipada de dados de forma estrita (como qualidade de DNA e atribuições em ferramentas).

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.component.TooltipDisplay;

*Onde encontrar:* `ModDataComponentTypes`, Livro Guia (`ArcheologyReimagined`), `DnaItem`.

## 3. Consumíveis e Efeitos (Consumables API)
Propriedades alimentares e aplicações de status (como poções ou alimentos com buffs/debuffs) agora seguem o novo paradigma unificado de componentes consumíveis.

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

*Onde encontrar:* `ModItems`, `BitterBerryBushBlock`.

## 4. Estados de Renderização de Entidades (Render States)
A lógica visual foi separada do objeto da entidade original em nível de servidor. A arquitetura de Render States (aliada ao GeckoLib atualizado) dita a renderização fluída e segura para o cliente.

// Estados de Renderização Vanilla e Fabric
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

// GeckoLib (Animações e Modelos)
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

*Onde encontrar:* Pacotes `com.lucas.arch.client.renderer` e `com.lucas.arch.client.model`.

## 5. Spawning, Navegação e Inteligência Artificial (Goals)
O sistema de eclosão de ovos, a lógica difusa baseada nos instintos (`Feelings` - raiva, medo, fome) exigem instâncias atualizadas de atributos e regras rigorosas de navegação.

// Spawning moderno
import net.minecraft.world.entity.EntitySpawnReason;

// Goals e Navegação
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

// Atributos base
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

*Onde encontrar:* `AbstractDinosaurEntity`, `AngerBehaviorGoal`, `CarnivoreHungerGoal`, `AbstractDinosaurEggBlockEntity`.

## 6. Codecs e Networking
Tráfego de pacotes entre Cliente-Servidor e a persistência de registros em disco agora utilizam Codecs de forma obrigatória.

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

*Onde encontrar:* `GuideBookRecipe`, `ModDataComponentTypes`.

## 7. Crafting e Receitas (Recipe API)
As matrizes de inputs para criação de itens em bancadas ou de forma customizada receberam melhorias para se desacoplarem de inventários genéricos.

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;

*Onde encontrar:* `GuideBookRecipe`, `ModRecipeSerializers`.

## 8. Arqueologia, Blocos e Tickers (Block Entities)
Blocos complexos que escaneiam ações a cada tick, bem como a mecânica de escavação (brushing) em Areia/Cascalho/Tufo.

import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.gameevent.GameEvent;

*Onde encontrar:* `ArchBrushableBlock`, `ArchBrushableBlockEntity`, blocos de processamento genérico.

## 9. Integração HUD/Tooltips (Jade/Waila)
Comunicação de dados do servidor diretamente para os overlays visuais do cliente (HUD de informações do dinossauro/processos na máquina).

import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

*Onde encontrar:* Todo o pacote `com.lucas.arch.compat.jade`.

## 10. Registros Base (Registries)
Ponto de partida central de qualquer mod Fabric para inserção no game loop.

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

*Onde encontrar:* Pacote `com.lucas.arch.registry`.