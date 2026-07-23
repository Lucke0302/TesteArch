# CODEMAP — ArcheologyReimagined

> **Objetivo deste arquivo:** mapear onde cada feature do mod vive no código, para
> qualquer pessoa (ou IA) que precise navegar o projeto sem ter que reler tudo do zero.
> Commitar na raiz do repo (ou em `/docs/CODEMAP.md`) e **atualizar sempre que uma
> classe/pacote novo for criado ou removido**.
>
> Gerado a partir do estado do repo em `main`, via GitHub API, em 23/07/2026.

---

## 1. Estrutura geral de pacotes

    com.lucas.arch
    ├── ArcheologyReimagined.java          → ModInitializer (bootstrap servidor+comum)
    ├── ArcheologyReimaginedClient.java    → ClientModInitializer (renderers, screens)
    ├── ArcheologyReimaginedDataGenerator.java  → datagen entrypoint (vazio, sem providers ainda)
    ├── ImplementedInventory.java          → interface default para block entities com inventário
    │
    ├── block/                             → classes de Block customizadas
    │   └── entity/                        → BlockEntity correspondentes
    ├── client/
    │   ├── model/                         → GeoModel (GeckoLib) do lado cliente
    │   └── renderer/                      → GeoEntityRenderer (GeckoLib)
    ├── config/                            → ModConfig + enums de configuração
    ├── entity/                            → entidades vivas (Animal/PathfinderMob)
    │   └── ai/                            → Goals customizadas de IA
    ├── item/                              → classes de Item customizadas
    ├── mixin/                             → mixins (injeção em classes vanilla)
    ├── recipe/                            → receitas customizadas (CustomRecipe) e catálogos
    ├── registry/                          → todos os `Registry.register(...)` do mod
    ├── screen/                            → Menu (Container) + Screen (GUI client-side)
    └── world/                             → worldgen (biome modifications, loot table mods)
        └── gen/                           → geradores de estrutura procedural (árvores etc.)

Recursos (`src/main/resources/`) seguem o padrão Fabric/Minecraft:
`assets/archeology_reimagined/{blockstates,models,items,textures,lang,geckolib}` e
`data/archeology_reimagined/{recipe,loot_table,worldgen,tags}`.

---

## 2. Mapa por feature

### 2.1 Ciclo de produção (Escavação → DNA → Embrião → Ovo)

| Etapa | Block | BlockEntity | Menu / Screen | Recipe/Loot |
|---|---|---|---|---|
| Mesa de Limpeza | `block/CleansingTableBlock.java` | `block/entity/CleansingTableBlockEntity.java` | `screen/CleansingTableMenu.java` + `CleansingTableScreen.java` | `recipe/ModCleansingRecipes.java` (catálogo Java) + `recipe/CleansingRecipe.java` (record) |
| Sintetizador | `block/SynthesizerBlock.java` | `block/entity/SynthesizerBlockEntity.java` | `screen/SynthesizerMenu.java` + `SynthesizerScreen.java` | lógica embutida em `synthesizeEmbryo()` |
| Fusor | `block/FuserBlock.java` | `block/entity/FuserBlockEntity.java` | `screen/FuserMenu.java` + `FuserScreen.java` | lógica embutida em `fuseEgg()` |

Todos os três blocks compartilham o mesmo padrão: `ContainerData` com 4-5 slots de
progresso/combustível, `serverTick()` no BlockEntity, e blockstate com propriedade
`lit` (exceto Cleansing Table, que não tem `lit`).

### 2.2 Nascimento / Entidade viva

| Peça | Arquivo |
|---|---|
| Entidade | `entity/AllosaurusEntity.java` — extends `Animal`, implementa `GeoEntity` (GeckoLib) |
| IA custom | `entity/ai/SeekDroppedFoodGoal.java` — persegue itens dropados da tag `carnivore_food` |
| Registro da entidade | `registry/ModEntities.java` |
| Modelo (cliente) | `client/model/AllosaurusModel.java` |
| Renderer (cliente) | `client/renderer/AllosaurusRenderer.java` — escala visual dinâmica via `DataTicket` |
| Assets GeckoLib | `assets/archeology_reimagined/geckolib/models/allosaurus.geo.json`, `geckolib/animations/allosaurus.animation.json` |
| Textura | `assets/archeology_reimagined/textures/entity/allosaurus_base.png` |
| Tag de comida | `data/archeology_reimagined/tags/item/carnivore_food.json` + `registry/ModTags.java` |

**Como funciona hoje:**
- Ao spawnar (`tick()` no `tickCount == 1`), sorteia cor (`COLORS[]`, ainda fixo em 3
  opções) e uma escala visual entre 2.7 e 3.5 (`SCALE` synced dado).
- Hitbox escala com a visual scale elevada a 0.9 (`getDefaultDimensions`).
- `finalizeSpawn` varia HP e dano de ataque em ±20% por indivíduo.
- Persegue comida solta no chão (`SeekDroppedFoodGoal`) e é atraída por ela
  (`TemptGoal`) — comida = itens na tag `carnivore_food` (carnes + `meat_cluster`).
- Anima idle/walk via `AnimationController` e tem um trigger de `attack`.
- **Não spawna sozinho no mundo** — falta amarrar o nascimento do `ALLOSAURUS_EGG`
  (chocar o ovo) a `EntityType.spawn(...)`.
- `getBreedOffspring` retorna `null` (sem reprodução).

### 2.3 Escavação / Escovação

| Peça | Arquivo |
|---|---|
| Block | `block/ArchBrushableBlock.java` — extends `BrushableBlock` vanilla |
| BlockEntity | `block/entity/ArchBrushableBlockEntity.java` — extends `BrushableBlockEntity` |
| Mixin de suporte | `mixin/BlockEntityMixin.java` — bypassa `validateBlockState` pro brushable custom funcionar |
| Registro | `registry/ModBlocks.java` (`BRUSHED_SAND/GRAVEL/TUFF`), `registry/ModBlockEntities.java` (`ARCH_BRUSHABLE_BE`) |
| Trigger de colocação | `ArcheologyReimagined.java` → listener em `UseBlockCallback.EVENT`: jogador usa `Items.BRUSH` em Areia/Cascalho/Tufo com espaço livre acima → vira o bloco escovável |

**Fluxo:** jogador escova → a cada "dusted" completo, 7.5% de chance de item raro
(fóssil aleatório ou Sniffer Egg, ver `dropRareItem()`) e destrói o bloco; senão dropa
um "pó" (`sand_powder`/`gravel_powder`/`tuff_powder`) e o bloco permanece.

### 2.4 Compactação de pós

- Itens: `SAND_POWDER`, `GRAVEL_POWDER`, `TUFF_POWDER` (`registry/ModItems.java`)
- Receitas 3x3 → bloco original: `data/.../recipe/sand_from_powder.json`,
  `gravel_from_powder.json`, `tuff_from_powder.json`

### 2.5 Botânica — Cica / Cycad (parcialmente implementado)

| Peça | Arquivo |
|---|---|
| Sapling | `block/CycadSaplingBlock.java` — farinha de osso (50% chance) chama `CycadFeature.generate()` |
| Estrutura adulta | `world/gen/CycadFeature.java` — planta `CYCAD_LOG` + `CYCAD_CENTER` (com fruto) + slabs de "folha" (usando `Blocks.OAK_SLAB` vanilla como placeholder) |
| Bloco central | `block/CycadCenterBlock.java` — propriedade `HAS_FRUIT`; farinha de osso regenera fruto (50%), interação sem item colhe 1-2 `CYCAD_FRUIT` |
| Itens | `CYCAD_SEED`, `CYCAD_FRUIT` (`registry/ModItems.java`) — fruto cru aplica **Veneno + Náusea** ao consumir (`ApplyStatusEffectsConsumeEffect`) |
| Receita | `data/.../recipe/cycad_seed_from_fruit.json` (fruto → 2 sementes) |

**Faltando:** plantar a semente (`CYCAD_SEED`) no mundo (hoje ela existe como item mas
não há bloco de "muda plantada a partir da semente" nem geração natural no worldgen);
texturas próprias (ainda usa `OAK_SLAB` como placeholder de folhagem); efeito de
"farinha de osso induz crescimento do fruto" já bate com a doc antiga.

### 2.6 Botânica — Sequoia Gigante (geração implementada)

| Peça | Arquivo |
|---|---|
| Sapling | `block/SequoiaSaplingBlock.java` — farinha de osso chama `SequoiaTreeFeature.generate()` direto (100% de chance, sem roll de falha) |
| Gerador procedural | `world/gen/SequoiaTreeFeature.java` — tronco 52-67 blocos, base em cruz 5x5 (4 primeiros blocos), corpo em corte 3x3, galhos + folhagem esférica (`generateFoliagePad`) e raízes espalhadas na base |

Usa `Blocks.OAK_LOG` / `Blocks.OAK_LEAVES` vanilla como placeholder — **falta a
textura**, mas a geração 3D está pronta.
Note: essa árvore só nasce via farinha de osso manual — **não há feature de worldgen
natural** registrada em `ModWorldGen.java` para ela nascer sozinha no bioma.

### 2.7 Bagas Amargas / Bitter Berries
| Peça | Arquivo |
|---|---|
| Block | `block/BitterBerryBushBlock.java` — extends `SweetBerryBushBlock` vanilla, com dano + lentidão ao pisar (`entityInside`) |
| Item | `item/ArchItemNameBlockItem.java` — classe que faz a ponte instanciando `ModItems.BITTER_BERRIES`, tornando-o comestível (aplica `SLOWNESS`) e permitindo plantio |
| Worldgen | `data/.../worldgen/configured_feature/bitter_berry_bush.json` + `placed_feature/bitter_berry_common.json` / `bitter_berry_rare.json` |
| Wiring dinâmico por bioma | `world/ModWorldGen.java` → lê `ModConfig.bitterBerryBiomes` (lista de biomas ou tags `#namespace:tag`) e registra a feature via `BiomeModifications` |

Isso é uma mudança de arquitetura importante: a lista de biomas onde a planta nasce
agora é **configurável via `config/archeology_reimagined.json`**, não hardcoded.

### 2.8 Fósseis, Âmbar, DNA
Ver seção de itens abaixo. Lógica de mineração/worldgen (`fossil_ore`, `amber_ore`,
densidade Low/Medium/High) e o mixin `FallingBlockEntityMixin` (fósseis caindo de
areia/cascalho/tufo) permanecem como estavam.

### 2.9 Utilitários químicos / contenção — sem mudança de status
`EMPTY_SYRINGE`, `FULL_SYRINGE`, `BIO_PROPELLANT`, `EMPTY_DART`, `FULL_DART`: ainda
são `ArchItem` genéricos (só craft + tooltip), **sem lógica de uso** implementada.
Isso continua sendo o maior gap entre a doc de design e o código.

### 2.10 Guia Arqueológico
`ArcheologyReimagined.createGuideBook()` agora tem **8 páginas**,
incluindo páginas novas sobre Escovação/Pincel, Botânica Exótica, Química Avançada e
Compactação. Receita de craft: `data/.../recipe/guide_book.json` via
`recipe/GuideBookRecipe.java`.

---

## 3. Registries (`com.lucas.arch.registry` e World)

| Classe | Registra / Propósito |
|---|---|
| `ModItems.java` | todos os itens (fósseis, DNA, combustíveis, pós, seringas, dardos, plantas...) |
| `ModBlocks.java` | todos os blocks + seus BlockItems (via `ArchBlockItem`) |
| `ModBlockEntities.java` | `CLEANSING_TABLE_BE`, `SYNTHESIZER_BE`, `FUSER_BE`, `ARCH_BRUSHABLE_BE` |
| `ModEntities.java` | `ALLOSAURUS` (EntityType) + `registerAttributes()` |
| `ModMenuTypes.java` | os 3 `MenuType` das máquinas |
| `ModRecipeSerializers.java` | `GUIDE_BOOK_RECIPE` (CustomRecipe) |
| `ModDataComponentTypes.java` | `DNA_QUALITY` (Integer, persistente + sincronizado) |
| `ModTags.java` | `Items.CARNIVORE_FOOD` (TagKey) |
| `world/ModLootTableModifiers.java` | Adiciona modificadores globais nas loot tables (injeção de drops customizados) |

## 4. Mixins (`com.lucas.arch.mixin`)

| Classe | Alvo | Propósito |
|---|---|---|
| `FallingBlockEntityMixin` | `FallingBlockEntity.onDestroyedOnLanding` | drop de fósseis quando bloco configurado (areia/cascalho/tufo) cai e quebra, modo worldgen "Reimagined" |
| `BlockEntityMixin` | `BlockEntity.validateBlockState` | bypass de validação para `ArchBrushableBlockEntity` funcionar com blockstates customizados |
| `ExampleMixin` | `MinecraftServer.loadLevel` | **template não usado**, candidato a remoção |

## 5. Config (`com.lucas.arch.config`)

- **`ModConfig.java`**: Classe principal. Campos conhecidos: `worldGenMode` (Classic/Original/Reimagined), `fossilDensity` (Low/Medium/High), tempos de processamento das 3 máquinas, `cleansingTableWaterCost`, `fossilDropChance` + `fossilDropBlocks` (lista), e o novo **`bitterBerryBiomes`** (lista de strings — id de bioma ou `#tag`). Serializado via Gson em `config/archeology_reimagined.json`, auto-regenerado se corrompido/faltando campos.
- **`WorldGenMode.java`**: Enum definindo as 3 abordagens de worldgen possíveis (Classic, Original, Reimagined).
- **`FossilDensity.java`**: Enum determinando a frequência de aparição de fósseis (Low, Medium, High).

## 6. Dependências relevantes (`build.gradle`)

- **GeckoLib** (`com.geckolib:geckolib-fabric-...`) via repositório Cloudsmith — usado
  só pela entidade Allosaurus por enquanto.
- Runtime-only: Sodium, Lithium, Ferrite Core, Jade, Spark, Mod Menu (via Modrinth maven).
- Java 25 / Minecraft `~26.2`.