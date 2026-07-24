#### Renderização Avançada de Pele (Grayscale + Overlay)
- [ ] Implementar a *Render Layer* de proteção no AllosaurusRenderer para garantir que olhos, garras e dentes não sejam tingidos pelo filtro de cor da pele.

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
  * *Sucesso:* Gera um **Ovo de Dinossauro chocável** (ex: `ALLOSAURUS_EGG`) herdando a qualidade final.
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

### 2.2 Comportamento & IA Customizada
* **Inteligência Alimentar (`SeekDroppedFoodGoal`):** A entidade detecta e persegue itens de comida dropados no chão marcados com a tag `#archeology_reimagined:carnivore_food` (carnes vanilla + `meat_cluster`).
* **Atração (`TemptGoal`):** Jogadores segurando itens da tag de carnívoros atraem a atenção do dinossauro.
* **Animações (GeckoLib 5):** Animações de `idle` e `walk` gerenciadas continuamente, com suporte a trigger de `attack`.

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
- [x] IA do Allossauro para caçar e buscar comida dropada no chão (`SeekDroppedFoodGoal`).
- [x] Arbusto de Bagas Amargas com dano, debuff e geração configurável por bioma via JSON.
- [x] Gerador procedural 3D da Sequóia Gigante via farinha de osso na muda.
- [x] Estrutura da planta Cica com ciclo de colheita e efeito nocivo no consumo do fruto.
- [x] Livro Guia com 8 páginas ilustradas.

---

### Parcialmente Implementado / Requer Ajustes
- [ ] **Mecanismo de Chocamento de Ovos:** O item `ALLOSAURUS_EGG` existe, mas ainda não é possível posicioná-lo no mundo para chocar o Allossauro automaticamente.
- [ ] **Placeholders de Botânica:**
  * A Sequóia e a Cica usam blocos vanilla (`OAK_LOG`, `OAK_LEAVES`, `OAK_SLAB`) como placeholder visual. Falta adicionar texturas e modelos próprios.
  * A Semente de Cica (`CYCAD_SEED`) existe como item, mas ainda não possui lógica/bloco para ser plantada no chão.
- [ ] **Geração Natural de Sequóias:** O gerador da Sequóia funciona via farinha de osso na muda, mas a árvore ainda não spawna naturally nos biomas via worldgen.

---

### A Fazer (Backlog de Features)

#### Sistema de Sentimentos & Personalidades (`Feelings & Traits`)
- [ ] **Traços de Personalidade:** Genética sorteada no nascimento (*Agressivo*, *Tímido*, *Guloso*, *Territorial*).
- [ ] **Estados Emocionais (Feelings):** Medidores contínuos de *Raiva*, *Fome*, *Medo* e *Curiosidade*.
- [ ] **Dinâmica das Goals:** Transições dinâmicas entre atração por comida, ataque ao jogador, fuga ou defesa de território com base no saldo emocional no instante.

#### Utilitários Químicos & Contenção (Mecânicas de Uso em Entidades)
- [ ] **Dardos Tranquilizantes (`FULL_DART`):** Implementar projétil disparável (arma/zarabatana ou arremesso) para sedar dinossauros à distância.
- [ ] **Seringas com Aditivos (`FULL_SYRINGE`):** Implementar interação no botão direito para injeção direta no dinossauro, aplicando mutações de atributos ou cura acelerada.

#### Sistema de Domesticação (Taming) & Vínculo
- [ ] Dinossauros nascem selvagens.
- [ ] Processo de domesticação alimentando filhotes recém-nascidos com alimentos nobres (`meat_cluster` / carnes) ou tranquilizados.
- [ ] Registro do UUID do jogador como Dono (`Owner`) da entidade.

#### Sela Customizada e Montaria
- [ ] Item de Sela de Dinossauro.
- [ ] **Trava de Segurança:** Apenas o dono pode montar na entidade.
- [ ] **Controle WASD:** Permitir que o jogador pilote o Allossauro, controlando direção, velocidade e ataque primário.

#### Renderização Avançada de Pele (Grayscale + Overlay)
- [ ] Implementar a *Render Layer* de proteção no AllosaurusRenderer para garantir que olhos, garras e dentes não sejam tingidos pelo filtro de cor da pele.