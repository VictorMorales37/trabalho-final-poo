package Entidades;

import Sistema.Movimentacao.Direcao;
import Sistema.Movimentacao.ResultadoMovimento;
import Sistema.Tabuleiro;

public class Jogador extends Entidade {
    private final char simbolo;
    private int percepcao;
    private int armaDardos;
    private int kitsMedicos;
    private boolean temBastao = false;

    public Jogador(char simbolo, int saude, int percepcao) {
        armaDardos = 0;
        kitsMedicos = 0;
        this.simbolo = simbolo;
        this.saude = saude;
        this.percepcao = percepcao;
        posicaoX = -1;
        posicaoY = -1;
    }

    public Jogador copia() {
        Jogador j = new Jogador(simbolo, saude, percepcao);
        j.setPosicaoX(posicaoX);
        j.setPosicaoY(posicaoY);
        return j;
    }

    @Override
    public char getSimbolo() {
        return simbolo;
    }

    public ResultadoMovimento mover(Direcao direcao, Tabuleiro tabuleiro) {
        int novoX = getPosicaoX() + direcao.dx;
        int novoY = getPosicaoY() + direcao.dy;

        ResultadoMovimento resultado = verificaMovimento(novoX, novoY, tabuleiro);

        if (resultado == ResultadoMovimento.LIVRE || resultado == ResultadoMovimento.ENCONTROU_CAIXA) {
            setPosicaoX(novoX);
            setPosicaoY(novoY);
        }
        return resultado;
    }

    public void setTemBastao() {
        temBastao = true;
    }

    public boolean getTemBastao() {
        return temBastao;
    }

    public void setArmaDardos(int armaDardos) {
        this.armaDardos += armaDardos;
    }

    public void setKitsMedicos(int kitsMedicos) {
        this.kitsMedicos = kitsMedicos;
    }

    public int getKitsMedicos() {
        return kitsMedicos;
    }

    public int getArmaDardos() {
        return armaDardos;
    }

    public void setPercepcao(int p) {
        percepcao = p;
    }

    public int getPercepcao() {
        return percepcao;
    }
}

