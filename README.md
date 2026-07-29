# Archeology Reimagined — Documento de Design & Status de Features

Um mod de arqueologia e biotecnologia pré-histórica focado em escavação, engenharia genética, catálise biológica, botânica e ressuscitação de criaturas da era mesozoica.

---

## O Ciclo de Produção & Clonagem

A progressão do mod é estruturada em etapas encadeadas para ressuscitar espécies extintas e sintetizar utilitários biotecnológicos:

### 1.1 Escavação e Coleta
* **Pincelamento em Superfície:** Ao usar um Pincel (`Items.BRUSH`) em blocos de Areia, Cascalho ou Tufo que tenham espaço livre acima, o bloco vira uma variante escovável customizada (`BRUSHED_SAND/GRAVEL/TUFF`).
  * Cada estágio "dusted" completo tem **7.5% de chance** de revelar um item raro (Fóssil Desconhecido aleatório ou Ovo de Sniffer) e destruir o bloco.
  * Caso contrário, dropa um "Pó" específico (`sand_powder`, `gravel_powder` ou `tuff_powder`) e o bloco permanece.
* **Mineração Subterrânea:** Bloco de *Fóssil* e *Minério de Âmbar* gerados no subsolo.
* **Queda de Blocos:** Mixin ativo (`FallingBlockEntityMixin`) que concede chance de drop de fósseis quando blocos de areia/cascalho/tufo caem e se quebram no modo "Reimagined".

### 1.2 Extração de DNA (Mesa de Limpeza)
* **Requisitos:** Consome **Água** (tanque até 10 baldes) e **Calor/Combustível** (fornalha vanilla).
* **Processamento:** Limpa Fósseis Desconhecidos, Mosquitos no Âmbar e **Seringas de Sangue (`BLOOD_SYRINGE`)**.
* **Resultado:** Produz amostras de **DNA Específico (Allosaurus, Spinosaurus, Pachycephalosaurus, Parasaurolophus, Quetzalcoatlus)** ou genérico (Mamíferos, Peixes, Plantas) com um atributo dinâmico de **Qualidade de DNA** (`DNA_QUALITY`, 0-100%). Em caso de falha no processo, gera *DNA Fragmentado* ou itens secundários (Areia, Cascalho, Osso, Carvão Vegetal).
* **Seringa de Sangue:** Ao processar uma `BLOOD_SYRINGE`, a mesa lê o `SYRINGE_SPECIES` do Data Component e gera **sempre o DNA da espécie correspondente** com **qualidade garantida entre 85% e 100%** (sem chance de falha).

### 1.3 Síntese de Embrião (Sintetizador)
* **Processamento:** Combina DNA específico (ex: `SPINOSAURUS_DNA`) com **Combustíveis Orgânicos**.
* **Modificadores de Qualidade:**
  * *Combustível Básico:* -15% de Qualidade no embrião final.
  * *Combustível Médio:* 0% de variação.
  * *Combustível Avançado:* +15% de Qualidade.
* **Resultado:** Em caso de sucesso, o maquinário lê a espécie do DNA e gera o Embrião correspondente (ex: `SPINOSAURUS_EMBRYO`). Em caso de falha na síntese, gera *Aglomerado de Carne* (`meat_cluster`).

### 1.4 Encapsulamento & Incubação (Fusor)
* **Processamento:** Funde o Embrião com ovos vanilla para criar uma casca viável.
* **Bônus de Viabilidade por Ovo:**
  * *Ovo de Galinha:* +0% bônus.
  * *Ovo de Tartaruga:* +15% bônus.
  * *Ovo de Sniffer:* +30% bônus.
* **Resultado:**
  * *Sucesso:* Gera um **Ovo de Dinossauro chocável** (`archeology_reimagined:allosaurus_egg_block`), pronto para ser plantado no mundo, herdando a qualidade final.
  * *Falha:* O ovo se rompe e produz de 3 a 6 unidades de *Aglomerado de Carne*.

### 1.5 Catálise Biológica & Utilitários (Biocatalisador)
* **Requisitos:** Requer 400 ticks (20 segundos) e consumo de combustíveis químicos/orgânicos estritos no slot inferior.
* **Rotas de Catálise:**
  * **Seringa com Aditivo:** DNA Fragmentado + Seringa Vazia nos slots superiores + **Combustível Orgânico Avançado** no slot de combustível $\rightarrow$ Gera 1 *Seringa com Aditivo* (`full_syringe`).
  * **Dardo Tranquilizante:** Frasco de Bagas Amargas (`bitter_berry_jar`) + Dardo Vazio nos slots superiores + **Biopropelente** no slot de combustível $\rightarrow$ Gera 1 *Dardo Tranquilizante* (`full_dart`) e substitui o Frasco de Bagas por um **Frasco de Vidro Vazio** (`glass_bottle`) no slot de entrada.
* **Crafting do Frasco de Bagas Amargas:** 8 Bagas Amargas ao redor de 1 Frasco de Vidro na Bancada de Trabalho.

---

## Entidades Vivas — Allossauro (`AllosaurusEntity`)

### 2.1 Genética & Aparência
* **Sorteio no Nascimento:** No primeiro tick de vida (`tickCount == 1`), a entidade sorteia:
  * **Cor:** Seleciona um valor Hexadecimal dentro da paleta da espécie (`COLORS[]`).
  * **Escala Visual:** Sorteia um multiplicador de tamanho entre **2.7x e 3.5x**.
* **Hitbox Dinâmica:** A dimensão física (`getDefaultDimensions`) escala proporcionalmente à escala visual elevada à potência de `0.9`.
* **Atributos Individuais:** No spawn (`finalizeSpawn`), HP Máximo e Dano de Ataque variam em **±20%** por indivíduo.

### 2.2 Comportamento & Motor Emocional

* **Sistema de Sentimentos:** A entidade acumula Raiva (`ANGER`), Medo (`FEAR`), Fome (`HUNGER`) e
  Curiosidade (`CURIOSITY`) com base em eventos do mundo (levar dano, tempo ocioso, saturação). O
  sentimento com maior peso ativo, ponderado pelas Traits (`updateDominantState`), se torna o **Estado
  Dominante** (`DOMINANT_STATE`, sincronizado e exibido no Jade).

* **Ciclo Circadiano (Sono Natural):**
  Cada espécie é definida como diurna ou noturna (`isDiurnal()`).
  O relógio biológico lê o tempo padrão do Overworld (`level.getDefaultClockTime()`).
  Durante o seu período de descanso (ex: de noite para diurnos), se a entidade estiver em **Estado Neutro** ou com sentimentos muito baixos ($\le 50\%$), ela tem 20% de chance a cada 10 segundos de deitar e dormir.
  
* **Instinto de Sobrevivência:** Se a fome atingir **80%** durante o sono (ou se a entidade for atacada), ela acorda imediatamente, ignorando o horário de descanso, para buscar alimento.

* **Resolução de Comportamento por Traits (`com.lucas.arch.entity.ai.BehaviorResolver`):** Para cada
  Feeling dominante existe **uma única Goal responsável** (`*BehaviorGoal`), evitando que múltiplas goals
  do mesmo Feeling disputem prioridade e gerem indecisão. Ao ativar, a goal pergunta ao
  `BehaviorResolver` qual das duas Traits mais fortes do dino "vence" para aquele Feeling, através de uma
  tabela de confronto **par-a-par** (não é uma ordem de ranking global — a tabela de `CURIOSITY` é
  intencionalmente não-transitiva). O comportamento resultante fica fixo durante toda a execução da goal,
  só sendo reavaliado na próxima ativação.

  **Faminto (`HUNGER` → `HungerBehaviorGoal`):**

  | Dominante × | Agressivo | Covarde | Curioso | Guloso |
  |---|---|---|---|---|
  | **Agressivo** | — | Agressivo | Agressivo | Agressivo |
  | **Covarde** | | — | Covarde | Guloso |
  | **Curioso** | | | — | Guloso |
  | **Guloso** | | | | — |

  Resultado por trait vencedor: *Agressivo* → caça/ataca jogadores com carne na mão; *Covarde/Curioso* →
  busca comida no chão; *Guloso* → tenta comida no chão primeiro, cai para caça se não encontrar.

  **Amedrontado (`FEAR` → `FearBehaviorGoal`):**

  | Dominante × | Agressivo | Covarde | Curioso | Guloso |
  |---|---|---|---|---|
  | **Agressivo** | — | Covarde | Agressivo | Agressivo |
  | **Covarde** | | — | Covarde | Covarde |
  | **Curioso** | | | — | *(empate)* |
  | **Guloso** | | | | — |

  *Agressivo* → ataca a ameaça em vez de fugir; *Covarde/Curioso/Guloso* → foge ativamente via
  pathfinder (`DefaultRandomPos`).

  **Irritado (`ANGER` → `AngerBehaviorGoal`):**

  | Dominante × | Agressivo | Covarde | Curioso | Guloso |
  |---|---|---|---|---|
  | **Agressivo** | — | Agressivo | Agressivo | Agressivo |
  | **Covarde** | | — | Curioso | Guloso |
  | **Curioso** | | | — | Guloso |
  | **Guloso** | | | | — |

  *Agressivo/Curioso/Guloso* → ataca o alvo mais próximo; *Covarde* → foge mesmo irritado.

  **Curioso (`CURIOSITY` → `CuriosityBehaviorGoal`):**

  | Dominante × | Agressivo | Covarde | Curioso | Guloso |
  |---|---|---|---|---|
  | **Agressivo** | — | Covarde | Curioso | *(empate)* |
  | **Covarde** | | — | Curioso | Guloso |
  | **Curioso** | | | — | Curioso |
  | **Guloso** | | | | — |

  *Agressivo/Guloso* → segue o jogador mais próximo; *Covarde* → não faz nada; *Curioso* → segue o dono
  se estiver domado e a até 16 blocos, senão segue o jogador mais próximo.

* **Coleira Invisível (`DinosaurFollowOwnerGoal`):** Dinossauros domados possuem uma mecânica de seguir o
  dono organicamente (caminhando em direção ao dono se afastados mais de 24 blocos), sem utilizar o
  teleporte quebra-imersão do Vanilla. Independente do sistema de Feelings.

### 2.3 Sistema de Áudio e Sincronia Animada (GeckoLib)
*   **Vanilla Hooks:** As entidades substituem os métodos nativos `getAmbientSound()`, `getHurtSound()` e `getDeathSound()` utilizando eventos registrados centralmente em `ModSounds.java`.
*   **SoundKeyframeHandler:** Efeitos incidentais complexos (ex: sons de passos pesados ou mastigação) não dependem de *ticks* no servidor. Eles são amarrados diretamente aos arquivos `.animation.json` criados no Blockbench através de strings de evento (`"effect": "eat"`), lidos e reproduzidos pelo controlador do GeckoLib via `setSoundKeyframeHandler`.
*   **Segurança de Thread (Desync Bounds Check):** Devido ao delay de sincronização do `EntityDataAccessor` entre o Servidor e a Thread de Renderização do Cliente (GeckoLib), a decodificação do estado emocional (Enum `Feeling`) possui um fallback de segurança (*bounds check*) que impede crashes (`IndexOutOfBoundsException`) ao carregar entidades recém-spawnadas.

### 2.3.1 Sincronização de Estado Visual (`isSleeping` / `isResting`)
*   **Correção (2026-07-28):** Os flags `isSleeping` e `isResting` (usados pelos `AnimationController` para
    decidir entre as animações `sleep`/`idle`/`walk`) eram campos Java comuns em `AbstractDinosaurEntity`,
    alterados apenas dentro de `tickTranquilizer()` e `NeutralBehaviorGoal#setResting()` — ambos executados
    exclusivamente na instância **server-side** da entidade.
*   **Sintoma:** A lógica de sono funcionava corretamente (navegação parava, alvo era limpo, `attachedDarts`
    decaía), mas a animação de dormir **nunca era exibida**, pois o GeckoLib avalia o `AnimationController`
    na instância **client-side**, que nunca recebia a mudança desses campos.
*   **Correção aplicada:** `isSleeping`/`isResting` foram promovidos a `EntityDataAccessor<Boolean>`
    (`IS_SLEEPING_SYNC` / `IS_RESTING_SYNC`) registrados em `defineSynchedData()`, seguindo o mesmo padrão já
    usado por `DOMINANT_STATE`, `IS_MALE` e `AGE_TIER_SYNC`. Os getters/setters públicos (`isSleeping()`,
    `isResting()`, `setResting(boolean)`) mantiveram a mesma assinatura, então nenhuma outra classe
    (`SleepBehaviorGoal`, `NeutralBehaviorGoal`, `FullDartItem`, os `registerControllers()` de cada espécie)
    precisou ser alterada.
*   **Nota de design:** os dois flags **não são persistidos** em `readAdditionalSaveData` — são estados
    transitórios de IA recalculados pelo tick do servidor, então um dino nunca deve "acordar dormindo" após
    um `/reload` ou relog do jogador.

### 2.4 Incubação do Ovo

O `Allosaurus Egg` produzido pelo Fusor é plantável no mundo. A eclosão depende de **calor constante** nos
6 blocos adjacentes:

| Fontes de calor adjacentes | Taxa de progresso |
|---|---|
| 0 | Pausado (não avança nem regride) |
| 1 | 1% a cada 200 ticks (~10s) |
| 5+ (saturação) | 1% a cada 50 ticks (~2.5s) |

Escala linearmente entre 1 e 5 fontes. Fontes reconhecidas: lava, magma block, fogueiras (normal/alma),
tochas (normal/alma/redstone, incluindo variantes de parede) e fogo.

Ao atingir 100%, o bloco é substituído por um Allossauro recém-nascido (`AgeTier.BABY`), herdando a
qualidade genética do DNA original processado no Fusor. Com o mod **Jade** instalado, a porcentagem de
eclosão é exibida no tooltip ao mirar no ovo.

* **Mecânica de Taming:** Filhotes possuem 50% de chance base de doma usando comidas carnívoras (`#archeology_reimagined:carnivore_food`), enquanto adultos possuem 10%. A chance flutua de acordo com a genética (Gula e Curiosidade aumentam a chance; Agressividade e Covardia diminuem).

### 2.5 Mecânica de Caça & Simulação de Alimentação

* **Caça Integrada ao `HungerBehaviorGoal`:** A caça ativa não é mais uma goal separada — ela é o
  sub-comportamento `HUNT_ATTACK` resolvido pelo `BehaviorResolver` dentro do estado de Fome (ver tabela
  em 2.2). Persegue Vacas, Porcos, Ovelhas e Galinhas num raio de 16 blocos, priorizando fauna sobre
  jogadores com carne na mão.
* **Consumo Dinâmico & Nutrição:** Ao abater uma presa em combate (`doHurtTarget`), o Allossauro simula o
  consumo imediato da carne correspondente ao mob (ex: Vaca $\rightarrow$ Beef, Ovelha $\rightarrow$
  Mutton, Allossauro $\rightarrow$ Meat Cluster).
* **Bônus de Abate:** Abates diretos garantem **2x mais nutrição** em saturação do que itens caídos do
  chão e aplicam cura imediata equivalente aos pontos de nutrição da carne.

### 2.6 Sistema de Atrofia por Desnutrição (`applyStuntedGrowthDebuff`)
* **Ciclo de Crescimento:** Requer **120.000 ticks** (~100 minutos) e acúmulo de **400.0 pontos de saturação** para progredir entre os estágios (`BABY` $\rightarrow$ `CHILD` $\rightarrow$ `JUVENILE` $\rightarrow$ `ADULT`).
* **Debuff de Atrofia:** Se atingir os 120.000 ticks sem a saturação necessária, a entidade passa a sofrer atrofia contínua:
  * A cada 600 ticks, recebe efeitos morais de `WEAKNESS` e `SLOWNESS` proporcionais ao tempo de atraso.
  * O estresse metabólico aumenta o sentimento de `ANGER` em +0.05 periodicamente.

### 2.7 Sistema Visual de Qualidade de DNA & Tooltips
* **Identificação Visual (`DnaItem`):** A qualidade do DNA codificada nos `DataComponents.DNA_QUALITY` altera dinamicamente a cor exibida no tooltip do item:
  * **< 55%:** Vermelho (`DARK_RED` / `RED`)
  * **55% - 69%:** Amarelo (`YELLOW`)
  * **70% - 84%:** Verde (`GREEN`)
  * **$\ge$ 85%:** Aqua (`AQUA`)

---

## Entidades Vivas — Pachycephalosaurus (`PachycephalosaurusEntity`)

O Pachycephalosaurus atua como a fundação arquitetural para os dinossauros herbívoros do mod. Ele reutiliza integralmente o Motor Emocional e a resolução de Traits do Allossauro, mas diverge no comportamento alimentar e atributos de combate.

### 2.8 Combate e Atributos
* **Design Defensivo:** Base HP 60 e Dano 6 (escalonável pela genética e idade). Ele não caça. A `AngerBehaviorGoal` utiliza uma animação de cabeçada (`attack_1`, `attack_2` ou `charge`) apenas para autodefesa.
* **Taming:** Utiliza a tag `#archeology_reimagined:herbivore_food`.

### 2.9 Dieta Herbívora e `PachycephalosaurusHungerGoal`
Diferente dos carnívoros, o Pachy não ataca outras entidades quando faminto. O `BehaviorResolver` foi projetado para ignorar `HUNT_ATTACK` neste contexto. 
A aquisição de nutrição segue uma ordem de prioridade estrita:
1. **Itens no chão:** Busca ativa por itens tageados como `HERBIVORE_FOOD` num raio de 16 blocos.
2. **Pastagem (Grazing):** Se não houver itens, ele procura blocos de grama (`grass_block`) próximos. Após 40 ticks de animação de comer (`eat`), converte o bloco em Terra (`dirt`) e concede um valor base de 3.0 de nutrição.

### 2.10 Motor Metábolico (`HERBIVORE_SATURATION_MULTIPLIER`)
Como herbívoros não recebem o "bônus de abate" do combate que os carnívoros possuem, o Pachycephalosaurus compensa isso aplicando um multiplicador global de **2.0x** (`HERBIVORE_SATURATION_MULTIPLIER`) para qualquer ganho de saturação (seja via item ou pastagem). O decréscimo da barra de `HUNGER` é escalonado com o mesmo multiplicador.

## Entidades Vivas — Spinosaurus (`SpinosaurusEntity`)

### 2.11 Combate, Dieta e Natação
* **Predador Semi-Aquático:** O Spinosaurus possui animações próprias de natação (`swim_underwater`) e se move de forma eficiente na água.
* **Caça e Nutrição:** Herda o comportamento carnívoro via `CarnivoreDiet` e `CarnivoreHungerGoal`. Sua lista de presas ativas inclui primariamente Peixes (`AbstractFish`), além de Ovelhas, Porcos e Galinhas. 
* **Abate:** Ao abater uma presa, o Spinosaurus simula o consumo imediato garantindo cura e saturação baseada em Bacalhau (`Items.COD`).
* **Design Ofensivo:** Maior hitbox (Max Scale 3.0), Base HP 90 e Dano 16.

## Entidades Vivas — Parasaurolophus (`ParasaurolophusEntity`)

### 2.12 Comportamento de Manada e Comunicação
*   **Herbívoro Social:** Compartilha a estrutura de dieta herbívora (`HERBIVORE_FOOD`) e conversão de `grass_block` do Pachycephalosaurus.
*   **Animações Específicas:** Possui um rig de animação avançado incluindo `speak` (chamados vocais usando a crista), `sit` (descanso passivo) e `sleep_adult` para transições de ciclo diário ou efeito de Dardos Tranquilizantes.
*   **Dimorfismo e Crescimento:** Texturas independentes para filhotes (`baby`), machos (`male`) e fêmeas (`female`), além do sistema genético padrão de variação de escala (Scale Modifier).

### 2.12.1 Correção do Mapeamento de Estado Dominante & Ciclo de "Olhar em Volta"
*   **Bug corrigido (2026-07-28):** `movementPredicate()` indexava `Feeling.values()[dominantStateByte]`
    sem subtrair 1, enquanto `DOMINANT_STATE` é gravado como `Feeling.ordinal() + 1` (0 = neutro). Isso
    deslocava todo o mapeamento em uma posição — o dino podia disparar `stand_up`/`stand_down` no estado
    emocional errado, ou nunca disparar quando realmente estava com medo ou fome. Corrigido para
    `Feeling.values()[dominantStateByte - 1]`, com `null` explícito representando o estado neutro.
*   **Nova animação idle — "olhar em volta":** aproveitando a correção, quando o Parasaurolophus está
    **neutro** (sem feeling dominante) e parado, ele agora entra periodicamente (a cada ~5-20s, por ~3-7s)
    num ciclo `stand_up` → `stand` → `stand_down`, reaproveitando exatamente as mesmas animações já usadas
    pelo estado de fome. O ciclo é interrompido automaticamente se ele começar a andar ou algum feeling
    assumir o controle. É puramente cosmético e local à instância renderizada (não sincronizado entre
    clientes/servidor — cada cliente decide seu próprio timing, como uma variação de idle qualquer).

## Botânica Pré-Histórica

* **Bagas Amargas (`BitterBerryBushBlock`):**
  * Arbusto espinhoso gerado em florestas/taigas (configurável via arquivo de config).
  * Prende entidades, causa dano de contato e aplica o efeito *Lentidão*.
  * Colheita produz `BITTER_BERRIES`, que podem ser consumidas pelo jogador para fome, mas aplicam *Lentidão*.
  * Permite criar o *Frasco de Bagas Amargas* (`bitter_berry_jar`) para catalisar compostos tranquilizantes.
* **Cica (`Cycad`):**
  * Estrutura gerada com tronco (`CYCAD_LOG`), bloco central (`CYCAD_CENTER`) e folhagem.
  * Farinha de osso no bloco central induz a regeneração do fruto (50% de chance).
  * Colheita produz de 1 a 2 `CYCAD_FRUIT`. Ingerir o fruto cru aplica **Veneno + Náusea**.
  * Craft de Fruto de Cica gera 2 Sementes de Cica (`CYCAD_SEED`).
* **Sequóia Gigante (`SequoiaTreeFeature`):**
  * Árvores procedurais colossais (52 a 67 blocos de altura).
  * Tronco estruturado com base em cruz 5x5 (4 primeiros blocos), corpo 3x3, raízes expostas e galhos altos com copas de folhagem arredondadas.

---

## Utilitários, Compactação e Guia

* **Compactação de Pós:** Receitas de bancada 3x3 para reconverter 9 Pós (`sand_powder`, `gravel_powder`, `tuff_powder`) de volta em blocos sólidos de Areia, Cascalho e Tufo.
* **Guia Arqueológico:** Item de livro customizado com 8 páginas interativas explicando escavação, funcionamento das máquinas, catálise biológica, botânica exótica e compactação.

---

## Status Geral de Implementação

### Concluído e Funcional
- [x] Maquinário básico completo (Mesa de Limpeza, Sintetizador, Fusor) com GUIs, sincronização de dados (`ContainerData`), consumo de combustíveis e barra de progresso.
- [x] Maquinário Biocatalisador (`BiocatalyzerBlock`) com validação estrita de pares ingrediente/combustível, produção de Seringas/Dardos e devolução automática de Frascos de Vidro no slot.
- [x] Item Frasco de Bagas Amargas (`bitter_berry_jar`) e receita de bancada associada.
- [x] Lógica de Pincelamento/Escovação customizada gerando pós e fósseis em areia/cascalho/tufo.
- [x] Receitas de compactação 3x3 de pós para blocos maciços.
- [x] Base da entidade Allossauro com GeckoLib 5 (RNG de escala 2.7-3.5x, paleta de cores, variação de atributos ±20%).
- [x] Sistema de Sentimentos & Personalidades: Genética de Traits (Agressividade, Covardia, Gula, Curiosidade) influenciando passivamente o Estado Dominante da entidade e sendo exibidos no Jade.
- [x] **Resolução de Comportamento por Traits (`BehaviorResolver`):** motor determinístico que resolve, para cada Feeling dominante, qual sub-comportamento executar via tabela de confronto par-a-par entre as duas Traits mais fortes do dino — substitui o antigo sistema de "Fuzzy Goals" concorrentes, eliminando o ciclo de indecisão em caça/fuga/ataque. Inclui `DinosaurFollowOwnerGoal` (Coleira Invisível sem teleporte) como goal independente de Feeling.
- [x] Caça ativa integrada ao `HungerBehaviorGoal` (sub-comportamento `HUNT_ATTACK`), com nutrição regenerativa de combate e bônus de cura por abate.
- [x] Sistema de Domesticação (Taming): Matemática de doma via comida carnívora influenciada diretamente pelas Traits genéticas da entidade.
- [x] Arbusto de Bagas Amargas com dano, debuff e geração configurável por bioma via JSON.
- [x] Gerador procedural 3D da Sequóia Gigante via farinha de osso na muda.
- [x] Estrutura da planta Cica com ciclo de colheita e efeito nocivo no consumo do fruto.
- [x] Livro Guia com 8 páginas ilustradas.
- [x] Mecânica de incubação do Ovo de Allossauro: bloco plantável, progresso dependente de fontes de calor adjacentes, nascimento automático do filhote ao atingir 100%.
- [x] Integração com Jade exibindo progresso de eclosão do ovo em tempo real.
- [x] Modelo 3D customizado em Blockbench para o Ovo de Allossauro (`allosaurus_egg_block.json`) montado em elementos geométricos.
- [x] Modelo 3D customizado com espinhos laterais para o Tronco de Cica (`CYCAD_LOG` / `cycad_log.json`).
- [x] Mecânica de atrofia muscular e debuffs em filhotes com fome estagnada.
- [x] Framework de autoria (`ArchItem`/`ArchBlockItem`) atribuindo designer e programador nos tooltips de todos os itens do mod.
- [x] Sistema de cores dinâmicas no tooltip para frascos e amostras de DNA baseado na qualidade genômica.
- [x] Biblioteca completa de animações GeckoLib 5 para o Allossauro no arquivo `allosaurus.animation.json` (`walk`, `run`, `idle`, `attack`, `eat`, `drink`, `sit`, `sleep`, `speak`, `swim`, `jump/fall`).
- [x] Entidade Pachycephalosaurus completa e conectada ao cliente (Model e Renderer em GeckoLib 5), operando com a IA de herbívoro baseada no Motor Emocional.
- [x] Casca física do Ovo de Pachycephalosaurus implementada (`Block`, `BlockEntity`, `BlockItem`), permitindo o plantio e a integração ao sistema de termodinâmica para incubação.
- [x] Refatoração dos provedores do mod Jade (`AllosaurusServerProvider` e `AllosaurusEggServerProvider`), atualizados para utilizar Pattern Matching e suportar genericamente as interfaces `FeelingDrivenEntity` e `AbstractDinosaurEggBlockEntity`.
- [x] Entidade Spinosaurus completa e conectada ao cliente (Model, Renderer em GeckoLib 5 e Animações aquáticas).
- [x] Casca física do Ovo de Spinosaurus implementada (`Block`, `BlockEntity`, `BlockItem`), com integração funcional à termodinâmica.
- [x] Expansão do pool de processamento da Mesa de Limpeza para distribuir DNAs de espécies de répteis designadas em vez de utilizar apenas placeholders de DNA padrão.
- [x] Lógica responsiva do Sintetizador implementada (reconhece `SPINOSAURUS_DNA` vs `ALLOSAURUS_DNA` e gera o embrião respectivo de cada árvore genética).
- [x] Estrutura completa de áudio implementada (`ModSounds.java`, `sounds.json` e arquivos `.ogg` indexados por entidade).
- [x] Sincronização de efeitos sonoros com animações do Blockbench através do `SoundKeyframeHandler` do GeckoLib 5 (ex: som de mastigação amarrado ao frame exato da animação).
- [x] Implementação de Bounds Check e Fallback state no `GeoRenderer`/`AnimationController` para evitar crashes por dessincronização de rede (Desync) entre o Client e o Server na leitura de variáveis de I.A. (`DOMINANT_STATE`).
- [x] **Seringa Vazia & Extração de Sangue:** Coleta de sangue de dinossauros (50% de chance), com reação de raiva em animais selvagens. Gera `BLOOD_SYRINGE` com Data Component `SYRINGE_SPECIES`.
- [x] **Processamento de Seringa de Sangue na Mesa de Limpeza:** `BLOOD_SYRINGE` é aceita como input e gera o DNA da espécie correspondente (Allosaurus, Spinosaurus, Pachycephalosaurus, Parasaurolophus) com qualidade garantida entre 85% e 100%.
- [x] **Correção da sincronização de `isSleeping`/`isResting` (2026-07-28):** flags promovidos para `EntityDataAccessor<Boolean>` sincronizados em `AbstractDinosaurEntity`, corrigindo a animação de sono do Dardo Tranquilizante que não era exibida no cliente (ver 2.3.1).
- [x] **Correção do mapeamento de estado dominante no Parasaurolophus + ciclo "olhar em volta" (2026-07-28):** `movementPredicate()` tinha índice fora de sincronia com `DOMINANT_STATE`, disparando `stand_up`/`stand_down` no estado emocional errado; corrigido e adicionado um ciclo idle periódico de "levantar a cabeça e olhar em volta" quando neutro (ver 2.12.1).

- [x] **Dardos Tranquilizantes (`FULL_DART`) e Modo de Sono:**
  * **Aplicação (Botão Direito):** O jogador aplica o dardo diretamente no dinossauro.
  * **Dosagem Física:** Requer 1 dardo para cada metro de altura da entidade (baseado no `getBbHeight()`).
  * **Efeito Tardio:** Leva 15 segundos (300 ticks) após a dose completa para o sedativo fazer efeito.
  * **Reação em Não-Donos:** Aplicar dardo em dinossauro que não é do jogador aumenta a raiva em `aggroTrait * 1.5` (1.5x o impacto da seringa). Se a agressividade for ≥ 0.5, o dino ataca o jogador.
  * **Devolução do Dardo Vazio:** Após o uso, o `FULL_DART` é consumido e um `EMPTY_DART` é devolvido ao inventário do jogador (ou dropado se o inventário estiver cheio). Isso premia futura implementação de arma de dardos à distância.
  * **Modo de Sono:** A entidade limpa sua IA de navegação, deita no chão (aciona a animação `sleep` via GeckoLib) e entra em dormência profunda.
  * **Duração Máxima:** O sono dura **3600 ticks (3 minutos)** — o dino acorda automaticamente ao fim desse período.
  * **Wake-on-Damage:** Se o dinossauro sofrer qualquer dano durante o sono (`hurtServer`), ele acorda imediatamente (zera contagem de dardos, timer e retorna à animação `idle`).
  * **SleepBehaviorGoal:** Goal dedicada (`com.lucas.arch.entity.ai.SleepBehaviorGoal`) com flags `MOVE | LOOK | JUMP` travadas, mantendo a animação de `sleep` ativa continuamente enquanto `isSleeping == true`.

  - [x] Adicionar 1 dinossauro Voador (Quetzalcoatlus).
---

### Parcialmente Implementado / Requer Ajustes
- [ ] **Placeholders de Botânica Restantes:**
  * O bloco do centro da Cica (`CYCAD_CENTER`) altera o estado de fruta utilizando slabs vanilla (`OAK_SLAB` / `JUNGLE_SLAB`).
  * A muda da Cica (`CYCAD_SAPLING`) e a muda da Sequóia (`SEQUOIA_SAPLING`) usam o modelo da muda de carvalho (`OAK_SAPLING`).
  * A Semente de Cica (`CYCAD_SEED`) existe como item, mas ainda não possui lógica/bloco para ser plantada diretamente no chão.

---

### A Fazer (Backlog de Features)

#### Utilitários Químicos & Contenção (Mecânicas de Uso em Entidades)
- [ ] **Dardos Tranquilizantes (`FULL_DART`):** Implementar projétil disparável (arma/zarabatana ou arremesso) para sedar dinossauros à distância.
- [ ] **Alimentadores Automáticos:** Blocos de comedouro que filtram dietas (Carnívoro/Herbívoro) para recintos planejados (Goal: `EatFromFeederGoal`).

#### Sela Customizada e Montaria
- [ ] Item de Sela de Dinossauro.
- [ ] **Trava de Segurança:** Apenas o dono pode montar na entidade.
- [ ] **Controle WASD:** Permitir que o jogador pilote o Allossauro, controlando direção, velocidade e ataque primário.

#### Renderização Avançada de Pele (Grayscale + Overlay)
- [ ] Implementar a *Render Layer* de proteção no AllosaurusRenderer para garantir que olhos, garras e dentes não sejam tingidos pelo filtro de cor da pele.

#### Utilitários Químicos & Contenção (Mecânicas de Uso em Entidades)
- [ ] **Melhoramento de Embriões (Seringa com Aditivo):**
  * Adicionar mecânica (provavelmente no Fusor ou numa nova interface/crafting) para consumir a `FULL_SYRINGE` e aumentar a % de qualidade de um embrião existente.
- [ ] **Drop Protegido Pós-Morte:** Dinossauros devem dropar carnes protegidas que não despawnam rapidamente, permitindo re-clonagem.
- [ ] **Quebra de Blocos (Breakout):** Dinossauros irritados ou grandes quebrando cercas e blocos frágeis.

#### Adições aos animais
- [ ] Adicionar 1 dinossauro Aquático (Dieta piscívora).