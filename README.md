# 🦕 Sobrevivência Jurássica

Trabalho intermediário da disciplina **Programação Orientada a Objetos (2026/1)** — UFPel.

Jogo de sobrevivência em grade, baseado em turnos, implementado em Java sem interface gráfica.

---

## 📋 Descrição

O jogador assume o papel de um humano preso em um parque infestado de dinossauros. O objetivo é eliminar todos os dinossauros antes de ser devorado. O mapa é gerado aleatoriamente a cada partida, com paredes, caixas de suprimentos e inimigos espalhados pelo cenário.

---

## 🗺️ Legenda do Tabuleiro

| Símbolo | Significado |
|---------|-------------|
| `P` | Jogador |
| `█` | Parede (obstáculo intransponível) |
| `C` | Compsognato |
| `T` | Troodonte |
| `V` | Velociraptor |
| `R` | Tiranossauro Rex |
| `X` | Caixa de Suprimentos |
| `?` | Posição fora da linha de visão |

---

## 🦖 Dinossauros

| Espécie | Saúde | Velocidade | Características |
|---------|-------|------------|-----------------|
| Compsognato | 1 | 1 | Pode se esconder em caixas de suprimentos |
| Troodonte | 2 | 1 | Inimigo comum, sem habilidades especiais |
| Velociraptor | 2 | 2 | Move-se 2 casas por turno; imune a dardos |
| Tiranossauro Rex | 3 | 0 | Não se move; exige arma para ser ferido; golpe causa 2 de dano |

O mapa começa com **2 Compsognatos, 5 Troodontes, 2 Velociraptors e 1 Tiranossauro Rex**. O T-Rex é sempre posicionado na extremidade oposta ao ponto de partida do jogador.

---

## 🎒 Itens

Existem 4 caixas de suprimentos no mapa, cada uma com um conteúdo:

- **Kit Médico** — recupera a saúde do jogador ao máximo
- **Bastão Elétrico** — substitui o ataque com as mãos; maior chance de acerto e dano
- **Arma de Dardos** — sempre causa golpe crítico (2 de dano), mas gasta 1 munição por uso; não funciona contra Velociraptors
- **Compsognato Surpresa** — um inimigo escondido ataca imediatamente ao abrir a caixa

---

## ⚔️ Sistema de Combate

### Ataque do jogador

| Arma | Dado | Crítico (2 dmg) | Falha (0 dmg) | Normal (1 dmg) |
|------|------|-----------------|---------------|----------------|
| Mãos | d6 | 6 | 1 ou 2 | 3, 4, 5 |
| Bastão Elétrico | d6 | 6 | 1 | 2, 3, 4, 5 |
| Dardos | — | sempre | — | — |

### Restrições de combate

- Mãos nuas e bastão **não causam dano ao T-Rex** (exige bastão equipado para o bastão funcionar)
- Dardos **não funcionam contra o Velociraptor** (ágil demais para ser atingido)

### Ataque do dinossauro

Após o ataque do jogador, se o inimigo ainda estiver vivo, ele contra-ataca. O jogador rola um **dado de 3 lados** e desvia se o resultado for menor ou igual à sua **percepção**:

- **Fácil** (percepção 3): desvia sempre
- **Médio** (percepção 2): desvia com 2 em 3 de chance
- **Difícil** (percepção 1): desvia com 1 em 3 de chance

### Fuga

A qualquer turno do combate é possível fugir, escolhendo uma direção adjacente livre de inimigos e paredes. Se estiver completamente encurralado, a fuga falha e o turno segue normalmente.

---

## 📐 Conceitos de POO Aplicados

- **Interface** — `Entidade` define o contrato comum a todas as entidades do jogo (`getPosicaoX`, `getSaude`, `getSimbolo`, etc.)
- **Herança** — `Dinossauro` é uma classe abstrata; `Compsognato`, `Troodonte`, `Velociraptor` e `TiranossauroRex` a estendem
- **Polimorfismo** — o `SistemaCombate` trata todos os dinossauros via referências `Dinossauro`, chamando `dino.golpeEspecial()`, `dino.exigeArma()` e `dino.imuneADardos()` que cada subclasse sobrescreve conforme necessário
- **Encapsulamento** — todos os atributos são privados/protegidos; acesso apenas via getters e setters
- **Composição** — `Jogo` compõe `Tabuleiro`, `SistemaMovimento`, `SistemaCombate`, `SistemaItens`, `Spawner`, `Menu` e `LeitorDeInput`

---

## 📁 Estrutura do Projeto

```
src/
├── Main.java
├── entidades/
│   ├── Entidade.java          (interface)
│   ├── Dinossauro.java        (classe abstrata)
│   ├── Compsognato.java
│   ├── Troodonte.java
│   ├── Velociraptor.java
│   ├── TiranossauroRex.java
│   ├── Jogador.java
│   ├── Caixa.java
│   └── TipoCaixa.java         (enum: KIT_MEDICO, BASTAO, ARMA_DARDOS, COMPSOGNATO_SURPRESA)
├── sistema/
│   ├── Jogo.java              (orquestrador principal)
│   ├── Tabuleiro.java         (grid, paredes, visão do jogador)
│   ├── SistemaCombate.java
│   ├── SistemaItens.java
│   ├── Spawner.java
│   ├── Menu.java
│   ├── LeitorDeInput.java
│   ├── ResultadoCombate.java  (enum: VENCEU, PERDEU, FUGIU)
│   └── movimentacao/
│       ├── SistemaMovimento.java
│       ├── Direcao.java       (enum: CIMA, BAIXO, ESQUERDA, DIREITA, INVALIDA)
│       └── ResultadoMovimento.java (enum: LIVRE, BLOQUEADO, ENCONTROU_DINOSSAURO, ENCONTROU_CAIXA)
└── util/
    └── Macros.java            (constantes globais do jogo)
```

---

## 🚀 Como Compilar e Executar

### Pré-requisitos

- **Java 21** ou superior (testado com OpenJDK 21)
- Sem dependências externas

### Via terminal

```bash
# Na pasta raiz do projeto (onde está src/)
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dstdout.encoding=UTF-8 -cp out Main
```

### Via IntelliJ IDEA

1. Abrir o projeto no IntelliJ (File → Open → selecionar a pasta do projeto)
2. Marcar a pasta `src` como Sources Root (botão direito → Mark Directory as → Sources Root)
3. Configurar o SDK para Java 21 (File → Project Structure → Project → SDK)
4. Executar `Main.java` com o botão ▶

> **Nota sobre encoding:** em terminais fora do IntelliJ, adicione `-Dstdout.encoding=UTF-8` à JVM para exibir corretamente os caracteres especiais (acentos e `█`).

---

## 🎮 Exemplo de Sessão

```
=========================================
   Bem-vindo a SOBREVIVÊNCIA JURÁSSICA!
=========================================
1- Jogar
2- Sair
Selecione a Dificuldade:
1- Fácil   (percepção 3)
2- Médio   (percepção 2)
3- Difícil (percepção 1)

Saúde: 5/5   |   Percepção: 2
Itens: Kits Médicos=0  Dardos=0  Bastão=Não

? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? P ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? . ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? . ? ? ? ? ? ? ? ? ? ?
? ? ? ? ? ? ? ? ? T ? ? ? ? ? ? ? ? ? ?

1- Movimentar
2- Cura
3- Modo DEBUG
4- Sair
```

---

## ⚙️ Configurações (Macros.java)

As constantes do jogo ficam centralizadas em `src/util/Macros.java` e podem ser ajustadas:

| Constante | Valor padrão | Descrição |
|-----------|-------------|-----------|
| `TAMANHO_TABULEIRO` | 20 | Dimensão do mapa (N×N) |
| `SAUDE_JOGADOR` | 5 | Saúde inicial do jogador |
| `NUM_COMPSOGNATO` | 2 | Quantidade de Compsognatos |
| `NUM_TROODONTE` | 5 | Quantidade de Troodontes |
| `NUM_VELOCIRAPTOR` | 2 | Quantidade de Velociraptors |
| `NUM_TREX` | 1 | Quantidade de T-Rexes |
| `DANO_DARDOS` | 2 | Dano fixo da arma de dardos |

---

## 👥 Autores
Victor Morales e Miguel Muller

Trabalho desenvolvido para a disciplina de Programação Orientada a Objetos — UFPel, 2026/1.

Professores: Felipe Marques e Rafael Burlamaqui.
