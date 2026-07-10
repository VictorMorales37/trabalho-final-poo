    package Sistema.Movimentacao;

    public enum Direcao {
        CIMA (-1, 0),
        BAIXO (1, 0),
        ESQUERDA (0, -1),
        DIREITA (0, 1),
        INVALIDA (0, 0);

        public final int dx;
        public final int dy;

        Direcao(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }
