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
* **Processamento:** Limpa Fósseis Desconhecidos e Mosquitos no Âmbar.
* **Resultado:** Produz amostras de **DNA (Répteis, Mamíferos, Peixes, Plantas)** com um atributo dinâmico de **Qualidade de DNA** (`DNA_QUALITY`, 0-100%). Em caso de falha no processo, gera *DNA Fragmentado* ou itens secundários (Areia, Cascalho, Osso, Carvão Vegetal).

### 1.3 Síntese de Embrião (Sintetizador)
* **Processamento:** Combina DNA puro com **Combustíveis Orgânicos**.
* **Modificadores de Qualidade:**
  * *Combustível Básico:* -15% de Qualidade no embrião final.
  * *Combustível Médio:* 0% de variação.
  * *Combustível Avançado:* +15% de Qualidade.
* **Resultado:** Em caso de sucesso, gera um Embrião (ex: `ALLOSAURUS_EMBRYO`). Em caso de falha na síntese, gera *Aglomerado de Carne* (`meat_cluster`).

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

### 2.2 Comportamento & Motor Emocional (Fuzzy AI)
* **Sistema de Sentimentos:** A entidade acumula Raiva, Medo, Fome e Curiosidade com base em eventos do mundo (levar dano, tempo ocioso). O sentimento com maior peso ativo se torna o **Estado Dominante**.
* **Behavior Tree (Fuzzy Goals):** As decisões da entidade não são estáticas. Elas dependem dos limiares de emoção:
  * *FuzzyHungerGoal:* Caça outros animais ou ataca jogadores agressivamente se a Fome for alta. Se dócil/curioso, apenas rodeia o jogador esperando ser alimentado.
  * *FuzzyAggressiveGoal:* Em picos de Raiva, aumenta o raio de detecção e a velocidade para atacar alvos ignorando a própria espécie.
  * *FuzzyFleeGoal:* Em picos de Medo (amplificado pela Covardia), usa o pathfinder para fugir ativamente de ameaças maiores.
  * *FuzzyCuriosityGoal:* Rodeia passivamente o jogador, drenando a curiosidade.
* **Coleira Invisível (`DinosaurFollowOwnerGoal`):** Dinossauros domados possuem uma mecânica de seguir o dono organicamente (caminhando em direção ao dono se afastados mais de 24 blocos), sem utilizar o teleporte quebra-imersão do Vanilla.

### 2.3 Incubação do Ovo

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

### 2.4 Mecânica de Caça & Simulação de Alimentação (`AllosaurusHuntPreyGoal` / `doHurtTarget`)
* **Gatilho de Caça:** Ativado via algoritmo de desejo de caça:
  $$\text{Desejo} = (\text{Fome} \times 2.0) + \text{Gula} + (\text{Agressividade} \times 0.5)$$
* **Alvos Selecionados:** Galinhas, Porcos, Ovelhas e Vacas num raio de 32 blocos.
* **Consumo Dinâmico & Nutrição:** Ao abater uma presa em combate (`doHurtTarget`), o Allossauro simula o consumo imediato da carne correspondente ao mob (ex: Vaca $\rightarrow$ Beef, Ovelha $\rightarrow$ Mutton, Allossauro $\rightarrow$ Meat Cluster). 
* **Bônus de Abate:** Abates diretos garantem **2x mais nutrição** em saturação do que itens caídos do chão e aplicam cura imediata equivalente aos pontos de nutrição da carne.

### 2.5 Sistema de Atrofia por Desnutrição (`applyStuntedGrowthDebuff`)
* **Ciclo de Crescimento:** Requer **120.000 ticks** (~100 minutos) e acúmulo de **400.0 pontos de saturação** para progredir entre os estágios (`BABY` $\rightarrow$ `CHILD` $\rightarrow$ `JUVENILE` $\rightarrow$ `ADULT`).
* **Debuff de Atrofia:** Se atingir os 120.000 ticks sem a saturação necessária, a entidade passa a sofrer atrofia contínua:
  * A cada 600 ticks, recebe efeitos morais de `WEAKNESS` e `SLOWNESS` proporcionais ao tempo de atraso.
  * O estresse metabólico aumenta o sentimento de `ANGER` em +0.05 periodicamente.

### 2.6 Sistema Visual de Qualidade de DNA & Tooltips
* **Identificação Visual (`DnaItem`):** A qualidade do DNA codificada nos `DataComponents.DNA_QUALITY` altera dinamicamente a cor exibida no tooltip do item:
  * **< 55%:** Vermelho (`DARK_RED` / `RED`)
  * **55% - 69%:** Amarelo (`YELLOW`)
  * **70% - 84%:** Verde (`GREEN`)
  * **$\ge$ 85%:** Aqua (`AQUA`)

---

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
- [x] Fuzzy Behavior Tree: Motor emocional gerenciando Fome, Fuga, Raiva e Curiosidade, incluindo `DinosaurFollowOwnerGoal` (Coleira Invisível sem teleporte).
- [x] Sistema de Domesticação (Taming): Matemática de doma via comida carnívora influenciada diretamente pelas Traits genéticas da entidade.
- [x] Arbusto de Bagas Amargas com dano, debuff e geração configurável por bioma via JSON.
- [x] Gerador procedural 3D da Sequóia Gigante via farinha de osso na muda.
- [x] Estrutura da planta Cica com ciclo de colheita e efeito nocivo no consumo do fruto.
- [x] Livro Guia com 8 páginas ilustradas.
- [x] Mecânica de incubação do Ovo de Allossauro: bloco plantável, progresso dependente de fontes de calor adjacentes, nascimento automático do filhote ao atingir 100%.
- [x] Integração com Jade exibindo progresso de eclosão do ovo em tempo real.
- [x] Modelo 3D customizado em Blockbench para o Ovo de Allossauro (`allosaurus_egg_block.json`) montado em elementos geométricos.
- [x] Modelo 3D customizado com espinhos laterais para o Tronco de Cica (`CYCAD_LOG` / `cycad_log.json`).
- [x] Caça ativa a mobs passivos (`AllosaurusHuntPreyGoal`) com nutrição regenerativa de combate e bônus de cura por abate.
- [x] Mecânica de atrofia muscular e debuffs em filhotes com fome estagnada.
- [x] Framework de autoria (`ArchItem`/`ArchBlockItem`) atribuindo designer e programador nos tooltips de todos os itens do mod.
- [x] Sistema de cores dinâmicas no tooltip para frascos e amostras de DNA baseado na qualidade genômica.
- [x] Biblioteca completa de animações GeckoLib 5 para o Allossauro no arquivo `allosaurus.animation.json` (`walk`, `run`, `idle`, `attack`, `eat`, `drink`, `sit`, `sleep`, `speak`, `swim`, `jump/fall`).

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
- [ ] **Seringas com Aditivos (`FULL_SYRINGE`):** Implementar interação no botão direito para injeção direta no dinossauro, aplicando mutações de atributos ou cura acelerada.

#### Sela Customizada e Montaria
- [ ] Item de Sela de Dinossauro.
- [ ] **Trava de Segurança:** Apenas o dono pode montar na entidade.
- [ ] **Controle WASD:** Permitir que o jogador pilote o Allossauro, controlando direção, velocidade e ataque primário.

#### Renderização Avançada de Pele (Grayscale + Overlay)
- [ ] Implementar a *Render Layer* de proteção no AllosaurusRenderer para garantir que olhos, garras e dentes não sejam tingidos pelo filtro de cor da pele.