# CODEMAP — ArcheologyReimagined

> **Objetivo:** Mapa de onde cada feature vive no código, para navegação rápida
> do projeto. Atualize sempre que classes/pacotes forem criados ou removidos.
>
> Última atualização: 2026-07-25 (substituído sistema Fuzzy Goals por BehaviorResolver + *BehaviorGoal)
---

## 1. Estrutura geral de pacotes

com.lucas.arch
    ├── ArcheologyReimagined.java          → ModInitializer
    ├── ArcheologyReimaginedClient.java    → ClientModInitializer
    ├── ArcheologyReimaginedDataGenerator.java  → datagen (vazio)
    ├── ImplementedInventory.java          → interface para inventários
    │
    ├── block/                             → blocos customizados
    │   └── entity/                        → BlockEntities (inclui AbstractDinosaurEggBlockEntity)
    ├── client/
    │   ├── model/                         → GeoModel (GeckoLib)
    │   └── renderer/                      → GeoEntityRenderer
    ├── compat/
    │   └── jade/                          → integração com Jade (tooltips in-world genéricos)
    ├── config/                            → ModConfig + enums
    ├── entity/                            → entidades vivas (FeelingDrivenEntity)
    │   └── ai/                            → goals customizadas + BehaviorResolver
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
| IA (Goals estruturais) | `entity/ai/DinosaurFollowOwnerGoal.java`, `entity/ai/DinosaurTemptGoal.java` |
| IA (Resolução de comportamento por Feeling) | `entity/ai/AbstractFuzzyGoal.java`, `entity/ai/BehaviorResolver.java`, `entity/ai/HungerBehaviorGoal.java`, `entity/ai/FearBehaviorGoal.java`, `entity/ai/AngerBehaviorGoal.java`, `entity/ai/CuriosityBehaviorGoal.java` |
| Enums | `entity/Trait.java`, `entity/AgeTier.java`, `entity/Feeling.java` |
| Registro | `registry/ModEntities.java` |
| Modelo (cliente) | `client/model/AllosaurusModel.java` — `getTextureResource()` retorna `null`; textura definida pelo renderer |
| Renderer (cliente) | `client/renderer/AllosaurusRenderer.java` |
| Assets | `assets/.../geckolib/models/allosaurus.geo.json`, `.../animations/allosaurus.animation.json` (Animações mapeadas) |
| Áudio  | `assets/archeology_reimagined/sounds.json` (Índice de áudio) e arquivos `.ogg` em `assets/.../sounds/entity/` |
| Texturas | `assets/.../textures/entity/allosaurus_baby.png`, `_male.png`, `_female.png` |
| Tags | `data/archeology_reimagined/tags/item/carnivore_food.json` |
| Ovo (bloco) | `block/AllosaurusEggBlock.java` + `block/entity/AllosaurusEggBlockEntity.java` + `item/AllosaurusEggBlockItem.java` |

#### 2.2.1 Sistema de IA — `com.lucas.arch.entity.ai`

A partir da migração de 2026-07-25, o sistema de "Fuzzy Goals" (uma goal por Feeling, competindo por
prioridade fixa no `GoalSelector`) foi **substituído** por um sistema de resolução determinística por
Trait, para eliminar o ciclo de indecisão que ocorria quando múltiplas goals do mesmo Feeling disputavam
ativação a cada avaliação do vanilla `GoalSelector`.

**Removidas** (não existem mais no código): `FuzzyHungerGoal`, `FuzzyFleeGoal`, `FuzzyAggressiveGoal`,
`FuzzyCuriosityGoal`, `SeekDroppedFoodGoal`, `AllosaurusHuntPreyGoal`.

| Classe | Papel |
|---|---|
| `AbstractFuzzyGoal.java` | Base abstrata: gerencia cooldown de checagem, threshold de ativação (`DOMINANT_STATE_THRESHOLD`-like via `>= 0.75f` pra ativar / `>= 0.65f` pra continuar) e decaimento de Feeling ao parar (`stop()`). Mantida como base de todas as `*BehaviorGoal`. |
| `BehaviorResolver.java` | Classe utilitária **estática e sem estado** (não extends `Goal`). Para um `Feeling` dominante, identifica as duas `Trait`s de maior valor do dino e resolve, via tabela de confronto par-a-par explícita por Feeling (não-transitiva — ver `resolveCuriosity`), qual Trait "vence". Mapeia o vencedor pra um `Behavior` enum: `HUNT_ATTACK`, `FLEE`, `SEEK_GROUND_FOOD`, `BEG_OWNER`, `FOLLOW_PLAYER`, `FOLLOW_OWNER_OR_NEAREST`, `DO_NOTHING`. Chamado **uma vez** dentro de `canFuzzyActivate()` de cada goal — nunca por tick, o modo fica fixo até a goal terminar. |
| `HungerBehaviorGoal.java` | Goal única pro `Feeling.HUNGER` (substitui `FuzzyHungerGoal` + `SeekDroppedFoodGoal`). `HUNT_ATTACK` persegue fauna (vaca/porco/ovelha/galinha) ou jogador com item de `ModTags.Items.CARNIVORE_FOOD` na mão; `SEEK_GROUND_FOOD` busca `ItemEntity` da mesma tag no chão; `BEG_OWNER` (trait GLUTTONY dominante) tenta comida no chão primeiro e cai pra caça se não achar. |
| `FearBehaviorGoal.java` | Goal única pro `Feeling.FEAR` (substitui `FuzzyFleeGoal`). Resolve entre `FLEE` (foge da ameaça mais próxima via `DefaultRandomPos.getPosAway`) e `HUNT_ATTACK` (trait AGGRESSIVENESS dominante vira confronto em vez de fuga). |
| `AngerBehaviorGoal.java` | Goal única pro `Feeling.ANGER` (substitui `FuzzyAggressiveGoal`). Resolve entre `HUNT_ATTACK` e `FLEE` (trait COWARDICE dominante foge mesmo irritado). |
| `CuriosityBehaviorGoal.java` | Goal única pro `Feeling.CURIOSITY` (substitui `FuzzyCuriosityGoal`). Resolve entre `FOLLOW_PLAYER` (segue jogador mais próximo), `FOLLOW_OWNER_OR_NEAREST` (segue dono se domado e a até 16 blocos, senão jogador mais próximo) e `DO_NOTHING` (trait COWARDICE dominante — não faz nada). |
| `DinosaurFollowOwnerGoal.java` | Inalterada — "coleira invisível" sem teleporte, independente de Feeling. |
| `DinosaurTemptGoal.java` | Inalterada — extends `TemptGoal` vanilla, restrita ao dono se domado. |
| `SleepBehaviorGoal.java` (Planejado) | Goal de altíssima prioridade (sobrepõe as Fuzzy Goals). Trava a navegação do dinossauro e força a animação de `sleep` enquanto a duração do tranquilizante estiver ativa na entidade. |

**`AllosaurusEntity.registerGoals()`** usa prioridades **fixas** no `GoalSelector` (não são recalculadas
em runtime — a variação de comportamento vem inteiramente do `BehaviorResolver`, não de reordenar
prioridade):

    1 → FearBehaviorGoal
    2 → AngerBehaviorGoal
    3 → DinosaurTemptGoal
    4 → AllosaurusHungerGoal
    7 → DinosaurFollowOwnerGoal
    8 → CuriosityBehaviorGoal
    9 → WaterAvoidingRandomStrollGoal (vanilla)
    10 → RandomLookAroundGoal (vanilla)

### 2.3 Entidade Pachycephalosaurus (Herbívoro)

| Peça | Arquivo |
|---|---|
| Entidade | `entity/PachycephalosaurusEntity.java` — extends `TamableAnimal`, implementa `GeoEntity` e `FeelingDrivenEntity`. |
| IA (Fome/Pastagem) | `entity/ai/PachycephalosaurusHungerGoal.java` — IA exclusiva de herbívoro; busca itens ou transforma `grass_block` em `dirt`. |
| IA (Compartilhada) | Reutiliza as mesmas goals de Feeling do Allossauro: `FearBehaviorGoal`, `AngerBehaviorGoal`, `CuriosityBehaviorGoal` e a estática `BehaviorResolver`. |
| Enums | Compartilha `Trait`, `AgeTier`, `Feeling`. |
| Registro | `registry/ModEntities.java`, `registry/ModBlocks.java`, `registry/ModBlockEntities.java` |
| Modelo (cliente) | `client/model/PachycephalosaurusModel.java` |
| Renderer (cliente) | `client/renderer/PachycephalosaurusRenderer.java` |
| Assets | Animações mapeadas: `walk`, `idle`, `attack_1`, `attack_2`, `charge`, `eat`. |
| Tags | `data/archeology_reimagined/tags/item/herbivore_food.json` |
| Ovo (bloco) | `block/PachycephalosaurusEggBlock.java` + `block/entity/PachycephalosaurusEggBlockEntity.java` + `item/PachycephalosaurusEggBlockItem.java` |

### 2.4 Entidade Spinosaurus (Carnívoro Semi-Aquático)

| Peça | Arquivo |
|---|---|
| Entidade | `entity/SpinosaurusEntity.java` — extends `AbstractDinosaurEntity`, implementa `CarnivoreDiet`. |
| IA (Compartilhada) | Usa as mesmas goals do Allosaurus: `FearBehaviorGoal`, `AngerBehaviorGoal`, `CuriosityBehaviorGoal`, `CarnivoreHungerGoal`. |
| Registro | `registry/ModEntities.java`, `registry/ModBlocks.java`, `registry/ModBlockEntities.java` |
| Modelo (cliente) | `client/model/SpinosaurusModel.java` |
| Renderer (cliente) | `client/renderer/SpinosaurusRenderer.java` |
| Assets | Animações mapeadas: `walk`, `idle`, `attack`, `eat`, `swim_underwater`. |
| Ovo (bloco) | `block/SpinosaurusEggBlock.java` + `block/entity/SpinosaurusEggBlockEntity.java` + `item/SpinosaurusEggBlockItem.java` |

### 2.5 Entidade Parasaurolophus (Herbívoro)

| Peça | Arquivo |
|---|---|
| Entidade | `entity/ParasaurolophusEntity.java` — extends `AbstractDinosaurEntity`, implementa `GeoEntity` e `FeelingDrivenEntity`. |
| IA (Compartilhada) | Utiliza a mesma infraestrutura de Herbívoros baseada no Pachycephalosaurus, incluindo busca por `HERBIVORE_FOOD` e o motor `BehaviorResolver`. |
| Registro | `registry/ModEntities.java`, `registry/ModBlocks.java`, `registry/ModBlockEntities.java`. |
| Modelo (cliente) | `client/model/ParasaurolophusModel.java` apontando para `parasaurolophus.geo.json`. |
| Renderer (cliente) | `client/renderer/ParasaurolophusRenderer.java`. |
| Assets (Animações) | Animações mapeadas em `parasaurolophus.animation.json`: `attack`, `eat`, `!eat`, `idle`, `run`, `sit`, `sleep_adult`, `speak`, `swim`, `walk`. |
| Assets (Texturas) | `entity/parasaurolophus_baby.png`, `_female.png`, `_male.png`. |
| Ovo (bloco) | `block/ParasaurolophusEggBlock.java` + `block/entity/ParasaurolophusEggBlockEntity.java` + `item/ParasaurolophusEggBlockItem.java`. |

### 2.6 Escavação / Pincelamento

| Peça | Arquivo |
|---|---|
| Bloco escovável | `block/ArchBrushableBlock.java` |
| BlockEntity | `block/entity/ArchBrushableBlockEntity.java` |
| Mixin | `mixin/BlockEntityMixin.java` |
| Registros | `registry/ModBlocks.java`, `ModBlockEntities.java` |
| Gatilho | `ArcheologyReimagined.java` → `UseBlockCallback.EVENT` |

Fluxo: escovar areia/cascalho/tufo → 7.5% chance de item raro, senão dropa pó.

### 2.7 Compactação de pós
- Itens: `SAND_POWDER`, `GRAVEL_POWDER`, `TUFF_POWDER` (`registry/ModItems.java`)
- Receitas 3x3 → bloco original em `data/.../recipe/sand_from_powder.json`, etc.

### 2.8 Botânica — Cica

| Peça | Arquivo |
|---|---|
| Sapling | `block/CycadSaplingBlock.java` |
| Estrutura | `world/gen/CycadFeature.java` |
| Bloco central | `block/CycadCenterBlock.java` |
| Itens | `CYCAD_SEED`, `CYCAD_FRUIT` (`registry/ModItems.java`) |
| Receita | `data/.../recipe/cycad_seed_from_fruit.json` |

**Faltando:** bloco para plantar a semente; texturas próprias (ainda usa `OAK_SLAB` placeholder).

### 2.9 Botânica — Sequóia Gigante

| Peça | Arquivo |
|---|---|
| Sapling | `block/SequoiaSaplingBlock.java` |
| Gerador | `world/gen/SequoiaTreeFeature.java` |

Usa blocos vanilla como placeholder. Funciona apenas via farinha de osso (sem worldgen natural).

### 2.10 Bagas Amargas

| Peça | Arquivo |
|---|---|
| Bloco | `block/BitterBerryBushBlock.java` |
| Item | `item/ArchItemNameBlockItem.java` (berries) |
| Frasco | `ModItems.BITTER_BERRY_JAR` |
| Receita do frasco | `data/.../recipe/bitter_berry_jar.json` |
| Worldgen | `data/.../worldgen/...`, config dinâmica via `ModConfig` |

### 2.11 Fósseis, Âmbar, DNA e Mapeamento

| Peça | Arquivo | Propósito |
|---|---|---|
| Itens Base | `registry/ModItems.java` | Instancia Fósseis, Âmbar, Mosquitos e Frascos genéricos. |
| Itens de DNA | `item/DnaItem.java` | Classe base que colore a % de viabilidade no tooltip dinamicamente. |
| Espécies Suportadas | `registry/ModItems.java` | `ALLOSAURUS_DNA`, `SPINOSAURUS_DNA`, `PACHYCEPHALOSAURUS_DNA`, `PARASAUROLOPHUS_DNA`. |
| Sorteio | `recipe/ModCleansingRecipes.java` | `rollReptileDna()` distribui as chances da Mesa de Limpeza extrair DNAs de espécies específicas. |
| Mapeamento de Embrião | `block/entity/SynthesizerBlockEntity.java` | `synthesizeEmbryo()` mapeia o DNA inserido para o `EMBRYO` da espécie correspondente. |
| Mixin de Queda | `mixin/FallingBlockEntityMixin.java` | Drop de fósseis desestruturados em quedas de blocos. |

### 2.12 Utilitários químicos / Catálise

| Peça | Arquivo |
|---|---|
| Processamento | `block/entity/BiocatalyzerBlockEntity.java` |
| Itens | `EMPTY_SYRINGE`, `FULL_SYRINGE`, `BIO_PROPELLANT`, `EMPTY_DART`, `FULL_DART`, `BITTER_BERRY_JAR` em `registry/ModItems.java` |
| Item de Sangue (Planejado) | `item/BloodSyringeItem.java` (A ser criado, gerenciará o Data Component de espécie). |
| Componente de Espécie | `registry/ModDataComponentTypes.java` (A ser registrado: `SYRINGE_SPECIES`). |

### 2.13 Guia Arqueológico

`ArcheologyReimagined.createGuideBook()` — 8 páginas. Receita: `recipe/GuideBookRecipe.java`.

### 2.14 Ovo do Allossauro (Incubação)

| Peça | Arquivo |
|---|---|
| Bloco | `block/AllosaurusEggBlock.java` — `EntityBlock`, sem colisão, ticker server-side |
| BlockEntity | `block/entity/AllosaurusEggBlockEntity.java` — lógica de eclosão |
| BlockItem | `item/AllosaurusEggBlockItem.java` — transfere `DNA_QUALITY` do item pro BlockEntity ao plantar |
| Tag de calor | `data/archeology_reimagined/tags/block/egg_heat_sources.json` → registrada em `ModTags.Blocks.EGG_HEAT_SOURCES` |
| Registro | `registry/ModBlocks.java` (`ALLOSAURUS_EGG_BLOCK`), `registry/ModBlockEntities.java` (`ALLOSAURUS_EGG_BE`) |

### 2.15 Integração Jade (compat/jade)

| Peça | Arquivo |
|---|---|
| Server provider (Blocks) | `compat/jade/AllosaurusEggServerProvider.java` — (Refatorado para suportar `AbstractDinosaurEggBlockEntity` genericamente) |
| Client provider (Blocks) | `compat/jade/AllosaurusEggClientProvider.java` |
| Server provider (Entities)| `compat/jade/AllosaurusServerProvider.java` — (Refatorado para extrair dados genéricos de `FeelingDrivenEntity` e `TamableAnimal`) |
| Client provider (Entities)| `compat/jade/AllosaurusClientProvider.java` |
| Plugin | `compat/jade/ArchJadePlugin.java` — `@WailaPlugin` (Registra provedores para Allosaurus e Pachycephalosaurus) |

### 2.16 Classes Base de Itens & Sistema de Autoria (`com.lucas.arch.item`)

| Classe | Propósito |
|---|---|
| `ArchItem.java` | Item base do mod. Injeta tooltips automáticos de autoria (*"Designed by X"*, *"Programmed by Y"*). |
| `ArchBlockItem.java` | `BlockItem` estendido com o mesmo sistema de autoria de `ArchItem`. |
| `ArchItemNameBlockItem.java` | `ItemNameBlockItem` para sementes/plantas com sistema de autoria. |
| `DnaItem.java` | Exibe tooltip dinâmico de `DNA_QUALITY` formatado em cores conforme a porcentagem (Vermelho, Amarelo, Verde, Aqua). |
| `EncyclopediaItem.java` | Item de enciclopédia interativa (placeholder via mensagem de sistema ao usar botão direito). |

### 2.17 Worldgen & Injeção de Loot Tables (`com.lucas.arch.world`)

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
| `ModSounds.java` | Eventos de som (SoundEvents), incluindo Vanilla hooks e GeckoLib Keyframes |
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