# CODEMAP — ArcheologyReimagined

> **Objetivo:** Mapa de onde cada feature vive no código, para navegação rápida
> do projeto. Atualize sempre que classes/pacotes forem criados ou removidos.
>
> Última atualização: 2026-07-25

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
| IA | `entity/ai/SeekDroppedFoodGoal.java` |
| Enums | `entity/Trait.java`, `entity/AgeTier.java`, `entity/Feeling.java` |
| Registro | `registry/ModEntities.java` |
| Modelo (cliente) | `client/model/AllosaurusModel.java` — `getTextureResource()` retorna `null`; textura definida pelo renderer |
| Renderer (cliente) | `client/renderer/AllosaurusRenderer.java` |
| Assets | `assets/.../geckolib/models/allosaurus.geo.json`, `.../animations/allosaurus.animation.json` |
| Texturas | `assets/.../textures/entity/allosaurus_baby.png`, `_male.png`, `_female.png` |
| Tags | `data/archeology_reimagined/tags/item/carnivore_food.json` |

**Detalhes importantes:**
- Ao spawnar, sorteia cor (`COLORS[]`, fixo em 3 opções) e escala visual (2.7-3.5).
- Hitbox escala com `visualScale ^ 0.9` (`getDefaultDimensions`).
- `finalizeSpawn` varia HP e ataque em ±20%.
- `SeekDroppedFoodGoal` faz a entidade buscar comida dropada (tag `carnivore_food`).
- `TemptGoal` atrai com itens da mesma tag.
- Animações: `idle`, `walk`, trigger de `attack`.
- **Texturas:** O renderer utiliza `RenderStateDataKey` (`IS_BABY_KEY`, `IS_MALE_KEY`) para escolher a textura correta independentemente do `GeoRenderState.isBaby`, que não é confiável.
- `getBreedOffspring` retorna `null` (sem reprodução).
- O método `setAgeTier()` atualiza tanto o campo `ageTier` quanto o dado sincronizado `AGE_TIER_SYNC`, garantindo persistência correta entre saves.

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
| `ModTags.java` | `Items.CARNIVORE_FOOD` |

---

## 4. Mixins (`com.lucas.arch.mixin`)

| Classe | Alvo | Propósito |
|---|---|---|
| `FallingBlockEntityMixin` | `FallingBlockEntity.onDestroyedOnLanding` | Drop de fósseis em quedas |
| `BlockEntityMixin` | `BlockEntity.validateBlockState` | Bypass para brushable custom |
| `ExampleMixin` | `MinecraftServer.loadLevel` | Template (remover) |

---

## 5. Config (`com.lucas.arch.config`)

- `ModConfig.java`: campos de worldgen, tempos de máquinas, biomas de bitter berries, etc.
- `WorldGenMode.java`, `FossilDensity.java`: enums de configuração.

---

## 6. Dependências

- GeckoLib 5 (Fabric)
- Sodium, Lithium, Ferrite Core, Jade, Spark, Mod Menu (runtime)
- Java 25 / Minecraft 1.21+