import Sistema.Jogo;
import Sistema.EstadoJogo;

public static void main(String[] args) {
    Jogo jogo = new Jogo();

    while (jogo.getEstado() != EstadoJogo.SAIR) {
        jogo.iniciarJogo();

        if (jogo.getEstado() == EstadoJogo.NOVO_JOGO) {
            jogo = new Jogo();
        }
    }
}