# Checklist de Implementação — Projeto Jurassic Park

## ATIVIDADE 1 — Inicialização do Jogo

### Menu Inicial

* [x] Exibir mensagem de boas-vindas.
* [x] Exibir menu principal:

  * [x] Jogar.
  * [x] Sair.

### Seleção de Dificuldade

* [x] Fácil → percepção = 3.
* [x] Médio → percepção = 2.
* [x] Difícil → percepção = 1.

### Geração do Mapa

* [x] Gerar tabuleiro aleatoriamente.
* [x] Posicionar o jogador.
* [x] Posicionar paredes.
* [x] Posicionar caixas de suprimentos.
* [x] Posicionar os dinossauros.

### Quantidade de Entidades

* [x] 2 Compsognatos.
* [x] 2 Velociraptors.
* [x] 5 Troodontes.
* [x] 1 Tiranossauro Rex.
* [x] 4 Caixas.

### Regras Especiais

* [x] O T-Rex deve surgir na extremidade oposta ao jogador.
* [x] Opção "Sair" encerra o programa.

---

## ATIVIDADE 2 — Jogo em Andamento

### Exibição do Jogo

* [x] Imprimir o mapa.
* [x] Exibir saúde do jogador.
* [x] Exibir percepção do jogador.

### Sistema de Visão

* [x] O jogador enxerga horizontalmente.
* [x] O jogador enxerga verticalmente.

### Menu Durante o Jogo

* [x] Cura.
* [x] DEBUG.
* [x] Sair.

### Cura

* [x] Verificar se o jogador possui kit médico.
* [x] Recuperar pontos de vida.
* [x] Consumir o item.
* [x] Contar como uma jogada.

### Modo DEBUG

* [x] Revelar todas as posições do mapa.
* [x] Mostrar todos os inimigos.

### Opção Sair

* [x] Encerrar o jogo atual.
* [x] Exibir:

  * [x] Reiniciar Jogo.
  * [x] Novo Jogo.

---

## Movimentação

### Jogador

* [x] Movimentação apenas na horizontal.
* [x] Movimentação apenas na vertical.
* [x] Impedir movimento diagonal.

### Dinossauros

* [x] Todos os dinossauros (exceto T-Rex) se movem após a jogada do jogador.
* [x] Movimento aleatório.
* [x] Não podem ocupar a mesma posição.

### Velociraptor

* [x] Move até duas posições.
* [x] Pode parar na primeira posição.
* [x] Inicia combate ao encontrar o jogador.
---

## Combate

### Combate ao Encontrar Dinossauro

* [x] Iniciar combate ao entrar na posição do inimigo.

### Turnos de Combate

* [x] Jogador ataca.
* [x] Dinossauro ataca.

### Continuação do Combate

* [x] Permitir continuar lutando.
* [x] Permitir fugir.

### Ataque Surpresa

* [x] Se um dinossauro encontrar o jogador:
* [x] O dinossauro ataca primeiro.

---

## Caixas de Suprimentos

* [x] Permitir caixa e dinossauro na mesma posição.
* [x] Abrir a caixa ao interagir.
* [x] Aplicar o item obtido.

---

## ATIVIDADE 3 — Final do Jogo

### Condições de Vitória

* [x] Derrotar todos os dinossauros.

### Condições de Derrota

* [x] Jogador morrer.

### Tela Final

* [x] Exibir mensagem de vitória.
* [x] Exibir mensagem de derrota.

### Opções Finais

* [x] Novo Jogo.
* [x] Reiniciar Jogo (opcional).

### Novo Jogo

* [x] Retornar ao menu inicial.
* [x] Permitir escolher dificuldade.
* [x] Gerar novo mapa.