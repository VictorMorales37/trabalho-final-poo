package Sistema;

import Util.Direcao;

public class ThreadJogador extends Thread {

    private final Jogo jogo;
    private final Direcao direcao;

    public ThreadJogador(Jogo jogo, Direcao direcao) {
        this.jogo = jogo;
        this.direcao = direcao;
    }

    @Override
    public void run() {
        jogo.executarMovimento(direcao);
    }
}
