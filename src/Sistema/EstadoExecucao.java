package Sistema;

import Util.EstadoJogo;

// Controla se o jogo está pausado, o estado geral e o cooldown do jogador
public class EstadoExecucao {

    private EstadoJogo estado = EstadoJogo.NOVO_JOGO;
    private boolean pausado = false;
    private boolean debug = false;
    private long ultimoMovimentoJogador = 0;
    private static final long INTERVALO_JOGADOR_MS = 350;

    public EstadoJogo getEstado() {
        return estado;
    }

    public void setEstado(EstadoJogo estado) {
        this.estado = estado;
    }

    public boolean isPausado() {
        return pausado;
    }

    public void setPausado(boolean pausado) {
        this.pausado = pausado;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void alternarDebug() {
        debug = !debug;
    }

    // jogo ainda pode receber movimentos
    public boolean podeJogar() {
        return estado == EstadoJogo.CONTINUAR && !pausado;
    }

    public boolean cooldownPronto() {
        return System.currentTimeMillis() - ultimoMovimentoJogador >= INTERVALO_JOGADOR_MS;
    }

    public void registrarMovimentoJogador() {
        ultimoMovimentoJogador = System.currentTimeMillis();
    }

    public void resetarParaPartida() {
        estado = EstadoJogo.CONTINUAR;
        pausado = false;
        debug = false;
        ultimoMovimentoJogador = 0;
    }
}
