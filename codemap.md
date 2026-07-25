# CODEMAP — ArcheologyReimagined

> **Objetivo:** Mapa de onde cada feature vive no código, para navegação rápida
> do projeto. Atualize sempre que classes/pacotes forem criados ou removidos.
>
> Última atualização: 2026-07-25 (adicionado sistema IA Fuzzy, Goals, e Taming)
---

## 1. Estrutura geral de pacotes

    com.lucas.arch
    ├── ArcheologyReimagined.java          → ModInitializer
    ├── ArcheologyReimaginedClient.java    → ClientModInitializer
    ├── ArcheologyReimaginedDataGenerator.java  → datagen (vazio)
    ├── ImplementedInventory.java          → interface para inventários
    │
    ├── block/                             → blocos customizados
    │   └── entity/                        → BlockEntities
    ├── client/
    │   ├── model/                         → GeoModel (GeckoLib)
    │   └── renderer/                      → GeoEntityRenderer
    ├── compat/
    │   └── jade/                          → integração com Jade (tooltips in-world)
    ├── config/                            → ModConfig + enums
    ├── entity/                            → entidades vivas
    │   └── ai/                            → goals customizadas
    ├── item/                              → itens customizados
    ├── mixin/                             → mixins
    ├── recipe/                            → receitas customizadas
    ├── registry/                          → todos os registros
    ├── screen/                            → Menus + Screens
    └── world/                             → worldgen
        └── gen/                           → geradores de estrutura

---

## 2. Mapa por feature

### 2.1 Máquinas de produção

| Máquina | Block | BlockEntity | Menu / Screen | Recipe |
|---|---|---|---|---|
| Mesa de Limpeza | `block/CleansingTableBlock.java` | `block/entity/CleansingTableBlockEntity.java` | `screen/CleansingTableMenu.java` + `CleansingTableScreen.java` | `recipe/ModCleansingRecipes.java` (catálogo) |
| Sintetizador | `block/SynthesizerBlock.java` | `block/entity/SynthesizerBlockEntity.java` | `screen/SynthesizerMenu.java` + `SynthesizerScreen.java` | lógica em `synthesizeEmbryo()` |
| Fusor | `block/FuserBlock.java` | `block/entity/FuserBlockEntity.java` | `screen/FuserMenu.java` + `FuserScreen.java` | lógica em `fuseEgg()` |
| Biocatalisador | `block/BiocatalyzerBlock.java` | `block/entity/BiocatalyzerBlockEntity.java` | `screen/BiocatalyzerMenu.java` + `BiocatalyzerScreen.java` | lógica estrita em `getActiveRecipeType()` e `craftItem()` |

Todas usam `ContainerData` para sincronização de progresso/combustível.

### 2.2 Entidade Allossauro

| Peça | Arquivo |
|---|---|
| Entidade | `entity/AllosaurusEntity.java` — extends `TamableAnimal`, implementa `GeoEntity` |
| IA (Goals) | `entity/ai/SeekDroppedFoodGoal.java`, `entity/ai/DinosaurFollowOwnerGoal.java`, `entity/ai/DinosaurTemptGoal.java`, `entity/ai/AllosaurusHuntPreyGoal.java` |
| IA (Fuzzy) | `entity/ai/AbstractFuzzyGoal.java`, `entity/ai/FuzzyHungerGoal.java`, `entity/ai/FuzzyAggressiveGoal.java`, `entity/ai/FuzzyFleeGoal.java`, `entity/ai/FuzzyCuriosityGoal.java` |
| Enums | `entity/Trait.java`, `entity/AgeTier.java`, `entity/Feeling.java` |
| Registro | `registry/ModEntities.java` |
| Modelo (cliente) | `client/model/AllosaurusModel.java` — `getTextureResource()` retorna `null`; textura definida pelo renderer |
| Renderer (cliente) | `client/renderer/AllosaurusRenderer.java` |
| Assets | `assets/.../geckolib/models/allosaurus.geo.json`, `.../animations/allosaurus.animation.json` (Animações: `walk`, `run`, `idle`, `attack`, `eat`, `drink`, `sit`, `sleep`, `speak`, `swim`, `jump/fall`) |
| Texturas | `assets/.../textures/entity/allosaurus_baby.png`, `_male.png`, `_female.png` |
| Tags | `data/archeology_reimagined/tags/item/carnivore_food.json` |
| Ovo (bloco) | `block/AllosaurusEggBlock.java` + `block/entity/AllosaurusEggBlockEntity.java` + `item/AllosaurusEggBlockItem.java` |

### 2.3 Escavação / Pincelamento

| Peça | Arquivo |
|---|---|
| Bloco escovável | `block/ArchBrushableBlock.java` |
| BlockEntity | `block/entity/ArchBrushableBlockEntity.java` |
| Mixin | `mixin/BlockEntityMixin.java` |
| Registros | `registry/ModBlocks.java`, `ModBlockEntities.java` |
| Gatilho | `ArcheologyReimagined.java` → `UseBlockCallback.EVENT` |

Fluxo: escovar areia/cascalho/tufo → 7.5% chance de item raro, senão dropa pó.

### 2.4 Compactação de pós
- Itens: `SAND_POWDER`, `GRAVEL_POWDER`, `TUFF_POWDER` (`registry/ModItems.java`)
- Receitas 3x3 → bloco original em `data/.../recipe/sand_from_powder.json`, etc.

### 2.5 Botânica — Cica

| Peça | Arquivo |
|---|---|
| Sapling | `block/CycadSaplingBlock.java` |
| Estrutura | `world/gen/CycadFeature.java` |
| Bloco central | `block/CycadCenterBlock.java` |
| Itens | `CYCAD_SEED`, `CYCAD_FRUIT` (`registry/ModItems.java`) |
| Receita | `data/.../recipe/cycad_seed_from_fruit.json` |

**Faltando:** bloco para plantar a semente; texturas próprias (ainda usa `OAK_SLAB` placeholder).

### 2.6 Botânica — Sequóia Gigante

| Peça | Arquivo |
|---|---|
| Sapling | `block/SequoiaSaplingBlock.java` |
| Gerador | `world/gen/SequoiaTreeFeature.java` |

Usa blocos vanilla como placeholder. Funciona apenas via farinha de osso (sem worldgen natural).

### 2.7 Bagas Amargas

| Peça | Arquivo |
|---|---|
| Bloco | `block/BitterBerryBushBlock.java` |
| Item | `item/ArchItemNameBlockItem.java` (berries) |
| Frasco | `ModItems.BITTER_BERRY_JAR` |
| Receita do frasco | `data/.../recipe/bitter_berry_jar.json` |
| Worldgen | `data/.../worldgen/...`, config dinâmica via `ModConfig` |

### 2.8 Fósseis, Âmbar, DNA

Itens em `registry/ModItems.java`. Mixin de queda: `mixin/FallingBlockEntityMixin.java`.

### 2.9 Utilitários químicos / Catálise

| Peça | Arquivo |
|---|---|
| Processamento | `block/entity/BiocatalyzerBlockEntity.java` |
| Itens | `EMPTY_SYRINGE`, `FULL_SYRINGE`, `BIO_PROPELLANT`, `EMPTY_DART`, `FULL_DART`, `BITTER_BERRY_JAR` em `registry/ModItems.java` |

### 2.10 Guia Arqueológico

`ArcheologyReimagined.createGuideBook()` — 8 páginas. Receita: `recipe/GuideBookRecipe.java`.

### 2.11 Ovo do Allossauro (Incubação)

| Peça | Arquivo |
|---|---|
| Bloco | `block/AllosaurusEggBlock.java` — `EntityBlock`, sem colisão, ticker server-side |
| BlockEntity | `block/entity/AllosaurusEggBlockEntity.java` — lógica de eclosão |
| BlockItem | `item/AllosaurusEggBlockItem.java` — transfere `DNA_QUALITY` do item pro BlockEntity ao plantar |
| Tag de calor | `data/archeology_reimagined/tags/block/egg_heat_sources.json` → registrada em `ModTags.Blocks.EGG_HEAT_SOURCES` |
| Registro | `registry/ModBlocks.java` (`ALLOSAURUS_EGG_BLOCK`), `registry/ModBlockEntities.java` (`ALLOSAURUS_EGG_BE`) |

### 2.12 Integração Jade (compat/jade)

| Peça | Arquivo |
|---|---|
| Server provider (Blocks) | `compat/jade/AllosaurusEggServerProvider.java` |
| Client provider (Blocks) | `compat/jade/AllosaurusEggClientProvider.java` |
| Server provider (Entities)| `compat/jade/AllosaurusServerProvider.java` |
| Client provider (Entities)| `compat/jade/AllosaurusClientProvider.java` |
| Plugin | `compat/jade/ArchJadePlugin.java` — `@WailaPlugin` |

### 2.13 Classes Base de Itens & Sistema de Autoria (`com.lucas.arch.item`)

| Classe | Propósito |
|---|---|
| `ArchItem.java` | Item base do mod. Injeta tooltips automáticos de autoria (*"Designed by X"*, *"Programmed by Y"*). |
| `ArchBlockItem.java` | `BlockItem` estendido com o mesmo sistema de autoria de `ArchItem`. |
| `ArchItemNameBlockItem.java` | `ItemNameBlockItem` para sementes/plantas com sistema de autoria. |
| `DnaItem.java` | Exibe tooltip dinâmico de `DNA_QUALITY` formatado em cores conforme a porcentagem (Vermelho, Amarelo, Verde, Aqua). |
| `EncyclopediaItem.java` | Item de enciclopédia interativa (placeholder via mensagem de sistema ao usar botão direito). |

### 2.14 Worldgen & Injeção de Loot Tables (`com.lucas.arch.world`)

| Arquivo | Propósito |
|---|---|
| `world/ModWorldGen.java` | Registra minérios de Fóssil e Âmbar no subsolo do Overworld (`UNDERGROUND_ORES`) e geração de Bagas Amargas conforme biomas configurados no `ModConfig`. |
| `world/ModLootTableModifiers.java` | Escuta `LootTableEvents.MODIFY` para injetar drops de fósseis desestruturados ao quebrar areia, cascalho e tufo no modo `REIMAGINED`. |

**Notas técnicas:**
- Desde MC 1.21.6, Jade **proíbe** a mesma classe implementar `IServerDataProvider` e `IComponentProvider`
  simultaneamente — por isso são duas classes separadas em vez de um enum único.
- Entrypoint `jade` declarado em `fabric.mod.json`.
- Dependência no `build.gradle`: **precisa** ser `modImplementation "maven.modrinth:jade:${project.jade_version}"`
  — `runtimeOnly` não expõe a API pro compilador (`snownee.jade.api` não resolve).

---

## 3. Registries (`com.lucas.arch.registry`)

| Classe | Responsabilidade |
|---|---|
| `ModItems.java` | Todos os itens |
| `ModBlocks.java` | Blocos + BlockItems |
| `ModBlockEntities.java` | BlockEntities das máquinas + brushable |
| `ModEntities.java` | EntityType + atributos |
| `ModMenuTypes.java` | MenuTypes das 4 máquinas |
| `ModRecipeSerializers.java` | `GUIDE_BOOK_RECIPE` |
| `ModDataComponentTypes.java` | `DNA_QUALITY` |
| `ModTags.java` | `Items.CARNIVORE_FOOD`, `Blocks.EGG_HEAT_SOURCES` |

---

## 4. Mixins (`com.lucas.arch.mixin`)

| Classe | Alvo | Propósito |
|---|---|---|
| `FallingBlockEntityMixin` | `FallingBlockEntity.onDestroyedOnLanding` | Drop de fósseis em quedas |
| `BlockEntityMixin` | `BlockEntity.validateBlockState` | Bypass para brushable custom |

---

## 5. Config (`com.lucas.arch.config`)

- `ModConfig.java`: campos de worldgen, tempos de máquinas, biomas de bitter berries, etc.
- `WorldGenMode.java`, `FossilDensity.java`: enums de configuração.

---

## 6. Dependências

- GeckoLib 5 (Fabric)
- Sodium, Lithium, Ferrite Core, Jade, Spark, Mod Menu (runtime)
- Java 25 / Minecraft 1.21+